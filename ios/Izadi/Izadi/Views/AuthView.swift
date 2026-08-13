import SwiftUI

/// Minimal sign-in for a single counselor. Stays signed in across launches;
/// only needed again after delete/reinstall (or sign out).
struct AuthView: View {
    @EnvironmentObject private var auth: AuthService

    @State private var email = ""
    @State private var password = ""
    @State private var isCreatingAccount = false
    @State private var appear = false

    var body: some View {
        SoftScreenBackground {
            VStack(spacing: 0) {
                Spacer(minLength: 40)

                VStack(spacing: 18) {
                    Image("IzadiLogo")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 120, height: 120)
                        .opacity(appear ? 1 : 0)
                        .offset(y: appear ? 0 : 12)

                    Text("Izadi")
                        .font(.izadi(.display))
                        .foregroundStyle(IzadiColor.ink)
                        .opacity(appear ? 1 : 0)

                    Text("Counselling")
                        .font(.izadi(.title))
                        .foregroundStyle(IzadiColor.sage)
                        .opacity(appear ? 1 : 0)

                    Text(isCreatingAccount
                          ? "Create the practice login once. Use this same email after any reinstall."
                          : "Sign in with the practice email. You’ll stay signed in on this phone.")
                        .font(.izadi(.body))
                        .foregroundStyle(IzadiColor.inkSoft)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 12)
                }
                .padding(.horizontal, 28)

                VStack(spacing: 16) {
                    FoamField(
                        title: "Email",
                        text: $email,
                        keyboard: .emailAddress,
                        autocapitalization: .never
                    )

                    VStack(alignment: .leading, spacing: 8) {
                        Text("PASSWORD")
                            .font(.izadi(.label))
                            .tracking(2.2)
                            .foregroundStyle(IzadiColor.sageSoft)
                        SecureField("", text: $password)
                            .font(.izadi(.body))
                            .foregroundStyle(IzadiColor.ink)
                            .textContentType(isCreatingAccount ? .newPassword : .password)
                            .padding(16)
                            .background(IzadiColor.foam)
                            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                    }

                    if let error = auth.errorMessage {
                        Text(error)
                            .font(.izadi(.bodyMedium))
                            .foregroundStyle(IzadiColor.roseDeep)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .multilineTextAlignment(.leading)
                            .fixedSize(horizontal: false, vertical: true)
                    }

                    PrimaryButton(
                        title: isCreatingAccount ? "Create practice login" : "Sign in",
                        enabled: canSubmit
                    ) {
                        Task {
                            if isCreatingAccount {
                                await auth.signUp(email: email, password: password)
                            } else {
                                await auth.signIn(email: email, password: password)
                            }
                        }
                    }

                    Button {
                        isCreatingAccount.toggle()
                        auth.errorMessage = nil
                    } label: {
                        Text(isCreatingAccount
                              ? "Already set up? Sign in"
                              : "First time? Create practice login")
                            .font(.izadi(.bodyMedium))
                            .foregroundStyle(IzadiColor.sage)
                    }
                    .buttonStyle(.plain)
                }
                .padding(28)
                .opacity(appear ? 1 : 0)
                .offset(y: appear ? 0 : 16)

                Spacer()
            }
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.65)) {
                appear = true
            }
        }
    }

    private var canSubmit: Bool {
        !email.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && password.count >= 6
    }
}
