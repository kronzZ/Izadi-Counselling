import SwiftUI

struct ClientSessionsView: View {
    @EnvironmentObject private var store: PracticeStore
    let client: Client
    @Binding var path: NavigationPath

    @State private var tab = 0
    @State private var now = Date()
    private let clock = Timer.publish(every: 30, on: .main, in: .common).autoconnect()

    var body: some View {
        SoftScreenBackground {
            VStack(alignment: .leading, spacing: 16) {
                BackButton { path.removeLast() }
                    .padding(.horizontal, 16)

                VStack(alignment: .leading, spacing: 16) {
                    Text(client.fullName)
                        .font(.izadi(.title))
                        .foregroundStyle(IzadiColor.ink)
                    Text("Sessions")
                        .font(.izadi(.label))
                        .tracking(2.2)
                        .foregroundStyle(IzadiColor.sageSoft)

                    PrimaryButton(title: "Book new session") {
                        path.append(AppRoute.bookSession(client.id))
                    }

                    SegmentedTabs(tabs: ["Upcoming", "Past"], selected: $tab)

                    ScrollView {
                        LazyVStack(spacing: 7) {
                            let list = tab == 0 ? upcoming : past
                            if list.isEmpty {
                                EmptyStateText(
                                    title: tab == 0 ? "No upcoming sessions" : "No past sessions",
                                    subtitle: tab == 0
                                        ? "Book a new session for this client above."
                                        : "Completed and cancelled sessions will show here."
                                )
                            } else {
                                ForEach(list) { session in
                                    SessionRowCard(
                                        session: session,
                                        awaitingWrapUp: SessionQueries.isAwaitingWrapUp(session, now: now)
                                    ) {
                                        path.append(AppRoute.sessionDetail(session.id))
                                    }
                                }
                            }
                        }
                        .padding(.bottom, 28)
                    }
                }
                .padding(.horizontal, 24)
            }
            .padding(.top, 8)
        }
        .toolbar(.hidden, for: .navigationBar)
        .onReceive(clock) { now = $0 }
    }

    private var clientSessions: [Session] {
        store.sessions.filter { $0.clientId == client.id }
    }

    private var upcoming: [Session] {
        SessionQueries.manageUpcoming(clientSessions)
    }

    private var past: [Session] {
        SessionQueries.managePast(clientSessions)
    }
}
