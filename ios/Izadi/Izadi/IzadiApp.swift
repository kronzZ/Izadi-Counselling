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
        return true
    }
}

@main
struct IzadiApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @StateObject private var auth = AuthService()
    @StateObject private var store = PracticeStore()

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
                        HomeView()
                            .environmentObject(auth)
                            .environmentObject(store)
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
                                }
                            }
                    } else {
                        AuthView()
                            .environmentObject(auth)
                    }
                }

                if showSplash {
                    SplashView()
                        .transition(.opacity)
                        .zIndex(1)
                }
            }
            .task {
                // Don't hang forever if Auth is slow/offline.
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
            }
        }
    }
}
