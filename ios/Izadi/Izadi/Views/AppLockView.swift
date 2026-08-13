import SwiftUI

/// Full-screen privacy lock shown until Face ID / Touch ID / passcode succeeds.
struct AppLockView: View {
    @EnvironmentObject private var appLock: AppLockService

    var body: some View {
        SoftScreenBackground(fullMint: true) {
            GeometryReader { geo in
                // Fixed positions (not Spacer-driven) so Face ID / privacy-cover
                // state changes don’t shove the logo or mint background sideways.
                ZStack {
                    VStack(spacing: 0) {
                        Image("IzadiLogo")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 140, height: 140)
                            .accessibilityHidden(true)

                        VStack(spacing: 8) {
                            Text("Izadi")
                                .font(.izadi(.display))
                                .foregroundStyle(IzadiColor.ink)

                            Text("Counselling")
                                .font(.izadi(.title))
                                .foregroundStyle(IzadiColor.sage)

                            Text(appLock.isUnlocked
                                  ? " "
                                  : "Unlock to open the practice")
                                .font(.izadi(.body))
                                .foregroundStyle(IzadiColor.inkSoft)
                                .multilineTextAlignment(.center)
                                .padding(.top, 4)
                                .opacity(appLock.isUnlocked ? 0 : 1)
                        }
                        .padding(.top, 28)
                    }
                    .frame(maxWidth: .infinity)
                    .position(x: geo.size.width / 2, y: geo.size.height * 0.38)

                    VStack(spacing: 12) {
                        Text(appLock.errorMessage ?? " ")
                            .font(.izadi(.bodyMedium))
                            .foregroundStyle(IzadiColor.roseDeep)
                            .multilineTextAlignment(.center)
                            .opacity(appLock.errorMessage == nil ? 0 : 1)
                            .frame(minHeight: 40)

                        Button {
                            Task { await appLock.authenticate() }
                        } label: {
                            HStack(spacing: 10) {
                                Image(systemName: unlockSymbol)
                                    .font(.system(size: 18, weight: .medium))
                                Text(appLock.isAuthenticating
                                      ? "Waiting…"
                                      : "Unlock with \(appLock.biometryLabel)")
                                    .font(.izadi(.boldBody))
                            }
                            .foregroundStyle(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(IzadiColor.sage)
                            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                        }
                        .disabled(appLock.isAuthenticating || appLock.isUnlocked)
                        .opacity(appLock.isUnlocked ? 0 : 1)
                        .allowsHitTesting(!appLock.isUnlocked)
                    }
                    .padding(.horizontal, 40)
                    .frame(maxWidth: .infinity)
                    .position(x: geo.size.width / 2, y: geo.size.height * 0.72)
                }
                .frame(width: geo.size.width, height: geo.size.height)
            }
        }
        .ignoresSafeArea()
    }

    private var unlockSymbol: String {
        switch appLock.biometryLabel {
        case "Face ID": return "faceid"
        case "Touch ID": return "touchid"
        case "Optic ID": return "opticid"
        default: return "lock.open"
        }
    }
}
