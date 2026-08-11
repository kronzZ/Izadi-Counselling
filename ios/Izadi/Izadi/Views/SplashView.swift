import SwiftUI

struct SplashView: View {
    @State private var visible = false

    var body: some View {
        ZStack {
            IzadiColor.softSky.ignoresSafeArea()

            VStack(spacing: 20) {
                Image("IzadiLogo")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 140, height: 140)
                Text("Izadi")
                    .font(.izadi(.display))
                    .foregroundStyle(IzadiColor.ink)
            }
            .opacity(visible ? 1 : 0)
            .scaleEffect(visible ? 1 : 0.96)
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.55)) {
                visible = true
            }
        }
    }
}
