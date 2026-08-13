import SwiftUI
import UIKit
import FirebaseCore

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }
        SquarePaymentCoordinator.shared.configureIfNeeded()
        CourtesyReminderService.shared.configure()
        return true
    }

    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        SquarePaymentCoordinator.shared.handleOpenURL(url)
    }
}

@main
struct IzadiApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @StateObject private var auth = AuthService()
    @StateObject private var store = PracticeStore()
    @StateObject private var appLock = AppLockService()

    @Environment(\.scenePhase) private var scenePhase
    @State private var showSplash = true

    var body: some Scene {
        WindowGroup {
            ZStack {
                Group {
                    if auth.isConfiguring {
                        SoftScreenBackground(fullMint: true) {
                            ProgressView()
                                .tint(IzadiColor.sage)
                        }
                    } else if auth.isSignedIn {
                        // Keep HomeView mounted while locked so navigation + form drafts
                        // survive multitasking (Face ID only overlays, does not remount).
                        HomeView()
                            .environmentObject(store)
                            .environmentObject(CourtesyReminderService.shared)
                            .environmentObject(SquarePaymentCoordinator.shared)
                            .environmentObject(appLock)
                            .allowsHitTesting(appLock.isUnlocked && !appLock.isPrivacyCovered)
                    } else {
                        AuthView()
                            .environmentObject(auth)
                            .onAppear { appLock.clearForSignedOut() }
                    }
                }

                if auth.isSignedIn && appLock.needsLockScreen && !showSplash {
                    AppLockView()
                        .environmentObject(appLock)
                        .transition(.opacity)
                        .zIndex(2)
                }

                if showSplash {
                    SplashView()
                        .transition(.opacity)
                        .zIndex(3)
                }
            }
            .onOpenURL { url in
                _ = SquarePaymentCoordinator.shared.handleOpenURL(url)
            }
            .onChange(of: scenePhase) { _, phase in
                guard !showSplash else { return }
                appLock.handleScenePhase(phase, isSignedIn: auth.isSignedIn)
            }
            .onAppear {
                if let uid = auth.uid {
                    store.start(uid: uid)
                }
            }
            .onChange(of: auth.uid) { _, uid in
                if let uid {
                    store.start(uid: uid)
                } else {
                    store.stop()
                    Task { await CourtesyReminderService.shared.cancelAll() }
                }
            }
            .onChange(of: store.sessions) { _, sessions in
                guard auth.isSignedIn else { return }
                Task {
                    await CourtesyReminderService.shared.requestAuthorizationIfNeeded()
                    await CourtesyReminderService.shared.sync(sessions: sessions)
                }
            }
            .onChange(of: auth.isSignedIn) { _, signedIn in
                if signedIn {
                    appLock.lock()
                    if !showSplash {
                        Task { await appLock.authenticate() }
                    }
                    Task {
                        await CourtesyReminderService.shared.requestAuthorizationIfNeeded()
                        await CourtesyReminderService.shared.sync(sessions: store.sessions)
                    }
                } else {
                    appLock.clearForSignedOut()
                }
            }
            .task {
                try? await Task.sleep(nanoseconds: 2_000_000_000)
                if auth.isConfiguring {
                    auth.markConfigured()
                }
            }
            .task {
                try? await Task.sleep(nanoseconds: 1_400_000_000)
                withAnimation(.easeOut(duration: 0.35)) {
                    showSplash = false
                }
                if auth.isSignedIn && !appLock.isUnlocked {
                    await appLock.authenticate()
                }
            }
        }
    }
}
