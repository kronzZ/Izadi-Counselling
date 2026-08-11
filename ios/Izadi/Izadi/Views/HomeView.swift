import SwiftUI

struct HomeView: View {
    @EnvironmentObject private var store: PracticeStore

    @State private var showHero = false
    @State private var showNav = false
    @State private var showWelcomeSms = false
    @State private var now = Date()
    @State private var path = NavigationPath()

    private let clock = Timer.publish(every: 30, on: .main, in: .common).autoconnect()

    var body: some View {
        NavigationStack(path: $path) {
            SoftScreenBackground {
                VStack(alignment: .leading, spacing: 0) {
                    // Hero: brand left, bird right — matches Android HomeScreen
                    VStack(alignment: .leading, spacing: 16) {
                        HStack(alignment: .center, spacing: 12) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Izadi")
                                    .font(.custom("OutfitThin-Light", size: 52, relativeTo: .largeTitle))
                                    .foregroundStyle(IzadiColor.ink)
                                    .lineLimit(1)
                                    .minimumScaleFactor(0.8)

                                Text("Counselling")
                                    .font(.izadi(.title))
                                    .foregroundStyle(IzadiColor.sage)
                                    .tracking(0.4)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)

                            Image("IzadiLogo")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 96, height: 96)
                                .offset(y: -4)
                                .accessibilityLabel("Izadi Counselling")
                        }

                        Text(DailyQuotes.today())
                            .font(.izadi(.body))
                            .italic()
                            .foregroundStyle(IzadiColor.inkSoft)
                            .padding(.trailing, 48)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                    .opacity(showHero ? 1 : 0)
                    .offset(y: showHero ? 0 : 14)
                    .padding(.top, 40)

                    // Upcoming sessions
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Your next 5 sessions")
                            .font(.izadi(.titleMedium))
                            .foregroundStyle(IzadiColor.ink)

                        upcomingPanel
                    }
                    .padding(.top, 20)
                    .opacity(showHero ? 1 : 0)
                    .frame(maxHeight: .infinity)

                    // Nav actions
                    VStack(spacing: 8) {
                        HomeNavButton(
                            title: "Clients",
                            systemImage: "person.2"
                        ) {
                            path.append(AppRoute.clients)
                        }
                        HomeNavButton(
                            title: "Manage sessions",
                            systemImage: "calendar"
                        ) {
                            path.append(AppRoute.sessions)
                        }
                        HomeNavButton(
                            title: "Payments",
                            systemImage: "doc.text"
                        ) {
                            path.append(AppRoute.payments)
                        }
                        HomeNavButton(
                            title: "Send welcome SMS",
                            systemImage: "message"
                        ) {
                            showWelcomeSms = true
                        }
                    }
                    .padding(.top, 14)
                    .padding(.bottom, 24)
                    .opacity(showNav ? 1 : 0)
                    .offset(y: showNav ? 0 : 10)
                }
                .padding(.horizontal, 24)
            }
            .toolbar(.hidden, for: .navigationBar)
            .navigationDestination(for: AppRoute.self) { route in
                destination(for: route)
            }
            .sheet(isPresented: $showWelcomeSms) {
                WelcomeSmsSheet(
                    template: store.welcomeSmsTemplate,
                    onTemplateChange: store.updateWelcomeSmsTemplate
                )
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

    private var upcomingPanel: some View {
        let upcoming = SessionQueries.upcoming(store.sessions)
        return Group {
            if upcoming.isEmpty {
                VStack(spacing: 6) {
                    Spacer(minLength: 0)
                    Text("No upcoming sessions")
                        .font(.izadi(.titleMedium))
                        .foregroundStyle(IzadiColor.sage)
                    Text("Your next five bookings will show here.")
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.inkSoft)
                        .multilineTextAlignment(.center)
                    Spacer(minLength: 0)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding(8)
                .background(IzadiColor.foam.opacity(0.55))
                .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
            } else {
                VStack(spacing: 6) {
                    ForEach(upcoming) { session in
                        Button {
                            path.append(AppRoute.sessionDetail(session.id))
                        } label: {
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(session.clientName)
                                        .font(.izadi(.body))
                                        .foregroundStyle(IzadiColor.ink)
                                    Text("\(session.shortDate) · \(session.friendlyTime)")
                                        .font(.izadi(.bodyMedium))
                                        .foregroundStyle(IzadiColor.inkSoft)
                                    if SessionQueries.isAwaitingWrapUp(session, now: now) {
                                        Text("Awaiting wrap-up")
                                            .font(.izadi(.label))
                                            .foregroundStyle(IzadiColor.roseDeep)
                                    }
                                }
                                Spacer()
                                Image(systemName: "chevron.right")
                                    .foregroundStyle(IzadiColor.sageSoft)
                            }
                            .padding(.horizontal, 14)
                            .padding(.vertical, 10)
                            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
                            .background(
                                SessionQueries.isAwaitingWrapUp(session, now: now)
                                ? IzadiColor.butter
                                : IzadiColor.foam.opacity(0.95)
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(8)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(IzadiColor.foam.opacity(0.55))
                .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
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
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
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
