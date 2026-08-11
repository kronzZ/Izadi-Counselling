import SwiftUI
import MessageUI

struct HomeView: View {
    @EnvironmentObject private var store: PracticeStore
    @EnvironmentObject private var auth: AuthService

    @State private var showHero = false
    @State private var showNav = false
    @State private var showWelcomeSms = false
    @State private var now = Date()
    @State private var path = NavigationPath()

    private let clock = Timer.publish(every: 30, on: .main, in: .common).autoconnect()

    var body: some View {
        NavigationStack(path: $path) {
            SoftScreenBackground {
                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 28) {
                        header
                            .opacity(showHero ? 1 : 0)
                            .offset(y: showHero ? 0 : 14)

                        quoteBlock
                            .opacity(showHero ? 1 : 0)

                        upcomingBlock

                        navGrid
                            .opacity(showNav ? 1 : 0)
                            .offset(y: showNav ? 0 : 10)
                    }
                    .padding(.horizontal, 24)
                    .padding(.top, 18)
                    .padding(.bottom, 36)
                }
            }
            .navigationDestination(for: AppRoute.self) { route in
                destination(for: route)
            }
            .sheet(isPresented: $showWelcomeSms) {
                WelcomeSmsSheet(
                    template: store.welcomeSmsTemplate,
                    onTemplateChange: store.updateWelcomeSmsTemplate
                )
            }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Menu {
                        Button("Sign out", role: .destructive) {
                            store.stop()
                            auth.signOut()
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                            .foregroundStyle(IzadiColor.sage)
                    }
                }
            }
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.6)) { showHero = true }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.09) {
                withAnimation(.easeOut(duration: 0.55)) { showNav = true }
            }
        }
        .onReceive(clock) { now = $0 }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 10) {
            Image("IzadiLogo")
                .resizable()
                .scaledToFit()
                .frame(width: 72, height: 72)
            Text("Izadi")
                .font(.izadi(.display))
                .foregroundStyle(IzadiColor.ink)
            Text("Counselling practice")
                .font(.izadi(.label))
                .tracking(2.2)
                .foregroundStyle(IzadiColor.sageSoft)
        }
    }

    private var quoteBlock: some View {
        Text(DailyQuotes.today())
            .font(.izadi(.body))
            .italic()
            .foregroundStyle(IzadiColor.inkSoft)
            .fixedSize(horizontal: false, vertical: true)
    }

    private var upcomingBlock: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("UPCOMING")
                .font(.izadi(.label))
                .tracking(2.2)
                .foregroundStyle(IzadiColor.sageSoft)

            let upcoming = SessionQueries.upcoming(store.sessions)
            if upcoming.isEmpty {
                EmptyStateText(
                    title: "No sessions booked",
                    subtitle: "When you book, the next five will appear here."
                )
            } else {
                ForEach(upcoming) { session in
                    SessionRowCard(
                        session: session,
                        awaitingWrapUp: SessionQueries.isAwaitingWrapUp(session, now: now)
                    ) {
                        path.append(AppRoute.sessionDetail(session.id))
                    }
                }
            }
        }
    }

    private var navGrid: some View {
        VStack(spacing: 12) {
            HomeNavButton(title: "Clients", systemImage: "person.2") {
                path.append(AppRoute.clients)
            }
            HomeNavButton(title: "Sessions", systemImage: "calendar") {
                path.append(AppRoute.sessions)
            }
            HomeNavButton(title: "Payments", systemImage: "doc.text") {
                path.append(AppRoute.payments)
            }
            HomeNavButton(title: "Welcome SMS", systemImage: "message") {
                showWelcomeSms = true
            }
        }
    }

    @ViewBuilder
    private func destination(for route: AppRoute) -> some View {
        switch route {
        case .clients:
            ClientsView(path: $path)
        case .newClient:
            NewClientView(path: $path)
        case .clientDetail(let id):
            if let client = store.clients.first(where: { $0.id == id }) {
                ClientDetailView(client: client, path: $path)
            } else {
                SectionPlaceholder(title: "Client not found", description: "That client isn't in the list.")
            }
        case .clientSessions(let id):
            if let client = store.clients.first(where: { $0.id == id }) {
                ClientSessionsView(client: client, path: $path)
            } else {
                SectionPlaceholder(title: "Client not found", description: "That client isn't in the list.")
            }
        case .sessions:
            ManageSessionsView(path: $path)
        case .newBookingChoice:
            NewBookingChoiceView(path: $path)
        case .bookingPickClient:
            ClientsView(path: $path, pickerMode: true)
        case .bookSession(let clientId):
            if let client = store.clients.first(where: { $0.id == clientId }) {
                BookSessionView(client: client, path: $path)
            } else {
                SectionPlaceholder(title: "Client not found", description: "That client isn't in the list.")
            }
        case .sessionDetail(let id):
            if let session = store.sessions.first(where: { $0.id == id }) {
                SessionDetailView(session: session, path: $path)
            } else {
                SectionPlaceholder(title: "Session not found", description: "That session isn't available.")
            }
        case .payments:
            PaymentsView(path: $path)
        }
    }
}

enum AppRoute: Hashable {
    case clients
    case newClient
    case clientDetail(String)
    case clientSessions(String)
    case sessions
    case newBookingChoice
    case bookingPickClient
    case bookSession(String)
    case sessionDetail(String)
    case payments
}

private struct HomeNavButton: View {
    let title: String
    let systemImage: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 14) {
                ZStack {
                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                        .fill(IzadiColor.bloom)
                        .frame(width: 44, height: 44)
                    Image(systemName: systemImage)
                        .foregroundStyle(IzadiColor.sage)
                }
                Text(title)
                    .font(.izadi(.titleMedium))
                    .foregroundStyle(IzadiColor.ink)
                Spacer()
                Image(systemName: "arrow.right")
                    .foregroundStyle(IzadiColor.sageSoft)
            }
            .padding(16)
            .background(IzadiColor.foam)
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}

struct SectionPlaceholder: View {
    let title: String
    let description: String

    var body: some View {
        SoftScreenBackground {
            VStack(alignment: .leading, spacing: 12) {
                Text(title)
                    .font(.izadi(.title))
                    .foregroundStyle(IzadiColor.ink)
                Text(description)
                    .font(.izadi(.body))
                    .foregroundStyle(IzadiColor.inkSoft)
                Spacer()
            }
            .padding(24)
        }
    }
}
