import SwiftUI

struct PaymentsView: View {
    @EnvironmentObject private var store: PracticeStore
    @Binding var path: NavigationPath

    @State private var tab = 0

    var body: some View {
        SoftScreenBackground {
            VStack(alignment: .leading, spacing: 16) {
                BackButton { path.removeLast() }
                    .padding(.horizontal, 16)

                VStack(alignment: .leading, spacing: 16) {
                    Text("Payments")
                        .font(.izadi(.title))
                        .foregroundStyle(IzadiColor.ink)

                    SegmentedTabs(tabs: ["Upcoming", "Paid"], selected: $tab)

                    let upcoming = SessionQueries.upcomingPayments(store.sessions)
                    let paid = SessionQueries.paidPayments(store.sessions)

                    Group {
                        if tab == 0 {
                            Text("Estimated · \(MoneyFormatting.formatAud(cents: SessionQueries.estimatedUpcomingRevenueDollars(count: upcoming.count) * 100))")
                                .font(.izadi(.bodyMedium))
                                .foregroundStyle(IzadiColor.inkSoft)
                        } else {
                            Text("Collected · \(MoneyFormatting.formatAud(cents: SessionQueries.collectedPaymentsCents(paid)))")
                                .font(.izadi(.bodyMedium))
                                .foregroundStyle(IzadiColor.inkSoft)
                        }
                    }

                    ScrollView {
                        LazyVStack(spacing: 10) {
                            let list = tab == 0 ? upcoming : paid
                            if list.isEmpty {
                                EmptyStateText(
                                    title: tab == 0 ? "No pending payments" : "No payments collected yet",
                                    subtitle: tab == 0
                                        ? "Booked sessions waiting for wrap-up appear here."
                                        : "Completed paid sessions appear here."
                                )
                            } else {
                                ForEach(list) { session in
                                    Button {
                                        path.append(AppRoute.sessionDetail(session.id))
                                    } label: {
                                        HStack {
                                            VStack(alignment: .leading, spacing: 4) {
                                                Text(session.clientName)
                                                    .font(.izadi(.titleMedium))
                                                    .foregroundStyle(IzadiColor.ink)
                                                Text(session.shortDate)
                                                    .font(.izadi(.bodyMedium))
                                                    .foregroundStyle(IzadiColor.inkSoft)
                                                if tab == 1, let summary = session.formattedPaidSummary {
                                                    Text(summary)
                                                        .font(.izadi(.bodyMedium))
                                                        .foregroundStyle(IzadiColor.sage)
                                                }
                                            }
                                            Spacer()
                                            Image(systemName: "chevron.right")
                                                .foregroundStyle(IzadiColor.sageSoft)
                                        }
                                        .padding(16)
                                        .background(IzadiColor.foam)
                                        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                                    }
                                    .buttonStyle(.plain)
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
    }
}
