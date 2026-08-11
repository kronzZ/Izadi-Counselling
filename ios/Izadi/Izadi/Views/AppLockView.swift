import SwiftUI

/// Full-screen privacy lock shown until Face ID / Touch ID / passcode succeeds.
struct AppLockView: View {
    @EnvironmentObject private var appLock: AppLockService

    var body: some View {
        SoftScreenBackground(fullMint: true) {
            VStack(spacing: 28) {
                Spacer()

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

                    Text("Unlock to open the practice")
                        .font(.izadi(.body))
                        .foregroundStyle(IzadiColor.inkSoft)
                        .multilineTextAlignment(.center)
                        .padding(.top, 4)
                }

                if let error = appLock.errorMessage {
                    Text(error)
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.roseDeep)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }

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
                .disabled(appLock.isAuthenticating)
                .padding(.horizontal, 40)
                .padding(.top, 8)

                Spacer()
                Spacer()
            }
            .padding(.horizontal, 24)
        }
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
