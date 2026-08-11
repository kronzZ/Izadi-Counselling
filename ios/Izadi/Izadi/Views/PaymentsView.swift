import SwiftUI

struct PaymentsView: View {
    @EnvironmentObject private var store: PracticeStore
    @Binding var path: NavigationPath

    private var paid: [Session] {
        SessionQueries.paidPayments(store.sessions)
    }

    var body: some View {
        SoftScreenBackground {
            VStack(alignment: .leading, spacing: 16) {
                BackButton { path.removeLast() }
                    .padding(.horizontal, 16)

                VStack(alignment: .leading, spacing: 16) {
                    Text("Payments")
                        .font(.izadi(.title))
                        .foregroundStyle(IzadiColor.ink)

                    Text("Collected · \(MoneyFormatting.formatAud(cents: SessionQueries.collectedPaymentsCents(paid)))")
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.inkSoft)

                    ScrollView {
                        LazyVStack(spacing: 10) {
                            if paid.isEmpty {
                                EmptyStateText(
                                    title: "No payments collected yet",
                                    subtitle: "Completed paid sessions appear here."
                                )
                            } else {
                                ForEach(paid) { session in
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
                                                if let summary = session.formattedPaidSummary {
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
