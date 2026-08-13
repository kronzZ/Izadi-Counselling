import SwiftUI

struct ManageSessionsView: View {
    @EnvironmentObject private var store: PracticeStore
    @Binding var path: NavigationPath

    @State private var tab = 0
    @State private var now = Date()
    private let clock = Timer.publish(every: 30, on: .main, in: .common).autoconnect()

    var body: some View {
        SoftScreenBackground {
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    BackButton { path.removeLast() }
                    Spacer()
                    CircularAddButton {
                        path.append(AppRoute.newBookingChoice)
                    }
                    .padding(.trailing, 8)
                }
                .padding(.horizontal, 16)

                VStack(alignment: .leading, spacing: 16) {
                    Text("Sessions")
                        .font(.izadi(.title))
                        .foregroundStyle(IzadiColor.ink)

                    SegmentedTabs(tabs: ["Upcoming", "Past"], selected: $tab)

                    ScrollView {
                        LazyVStack(spacing: 7) {
                            let list = tab == 0
                                ? SessionQueries.manageUpcoming(store.sessions)
                                : SessionQueries.managePast(store.sessions)

                            if list.isEmpty {
                                EmptyStateText(
                                    title: tab == 0 ? "Nothing upcoming" : "No past sessions yet",
                                    subtitle: tab == 0
                                        ? "Book a session to see it here."
                                        : "Completed and cancelled sessions appear here."
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
}

struct NewBookingChoiceView: View {
    @Binding var path: NavigationPath

    var body: some View {
        SoftScreenBackground {
            VStack(alignment: .leading, spacing: 16) {
                BackButton { path.removeLast() }
                    .padding(.horizontal, 16)

                VStack(alignment: .leading, spacing: 16) {
                    Text("New booking")
                        .font(.izadi(.title))
                        .foregroundStyle(IzadiColor.ink)
                    Text("Book for someone new, or an existing client.")
                        .font(.izadi(.body))
                        .foregroundStyle(IzadiColor.inkSoft)

                    PrimaryButton(title: "New client") {
                        path.append(AppRoute.newClient)
                    }
                    Button {
                        path.append(AppRoute.bookingPickClient)
                    } label: {
                        Text("Existing client")
                            .font(.izadi(.boldBody))
                            .foregroundStyle(IzadiColor.ink)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(IzadiColor.foam)
                            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                    }
                    .buttonStyle(.plain)
                }
                .padding(.horizontal, 24)

                Spacer()
            }
            .padding(.top, 8)
        }
        .toolbar(.hidden, for: .navigationBar)
    }
}
