import SwiftUI

/// Matches Android splash: full-bleed mint, large centered bird, credit at bottom.
struct SplashView: View {
    @State private var visible = false

    var body: some View {
        ZStack {
            IzadiColor.softSky.ignoresSafeArea()

            LinearGradient(
                colors: [
                    IzadiColor.sageSoft.opacity(0.22),
                    IzadiColor.sageSoft.opacity(0.10),
                    IzadiColor.sageSoft.opacity(0.16),
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            Image("IzadiLogo")
                .resizable()
                .scaledToFit()
                .frame(width: 300, height: 300)
                .opacity(visible ? 1 : 0)
                .scaleEffect(visible ? 1 : 0.96)
                .accessibilityHidden(true)

            Text("© TurtleTech Designs 2026")
                .font(.izadi(.bodyMedium))
                .foregroundStyle(IzadiColor.inkSoft.opacity(0.75))
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
                .padding(.bottom, 20)
                .opacity(visible ? 1 : 0)
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.55)) {
                visible = true
            }
        }
    }
}
