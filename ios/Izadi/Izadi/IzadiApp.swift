import SwiftUI
import FirebaseCore

@main
struct IzadiApp: App {
    @StateObject private var auth = AuthService()
    @StateObject private var store = PracticeStore()

    @State private var showSplash = true

    init() {
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
            ZStack {
                Group {
                    if auth.isConfiguring {
                        SoftScreenBackground(fullMint: true) { ProgressView() }
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
                try? await Task.sleep(nanoseconds: 1_400_000_000)
                withAnimation(.easeOut(duration: 0.35)) {
                    showSplash = false
                }
            }
        }
    }
}
