import SwiftUI

struct SessionDetailView: View {
    @EnvironmentObject private var store: PracticeStore
    let session: Session
    @Binding var path: NavigationPath

    @State private var draft: Session
    @State private var showCompleteSheet = false
    @State private var paymentDollars = "150"
    @State private var completeError: String?

    init(session: Session, path: Binding<NavigationPath>) {
        self.session = session
        self._path = path
        self._draft = State(initialValue: session)
    }

    var body: some View {
        SoftScreenBackground {
            VStack(alignment: .leading, spacing: 0) {
                BackButton { path.removeLast() }
                    .padding(.horizontal, 16)

                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        Text(draft.clientName)
                            .font(.izadi(.title))
                            .foregroundStyle(IzadiColor.ink)
                        Text(draft.status.label.uppercased())
                            .font(.izadi(.label))
                            .tracking(2.2)
                            .foregroundStyle(IzadiColor.sageSoft)

                        Text(draft.friendlyDate)
                            .font(.izadi(.body))
                            .foregroundStyle(IzadiColor.ink)
                        Text("\(draft.friendlyTime) · \(draft.durationMinutes) minutes")
                            .font(.izadi(.bodyMedium))
                            .foregroundStyle(IzadiColor.inkSoft)

                        if let paid = draft.formattedPaidSummary {
                            Text(paid)
                                .font(.izadi(.bodyMedium))
                                .foregroundStyle(IzadiColor.sage)
                        }

                        notesField

                        if draft.status == .scheduled {
                            PrimaryButton(title: "Mark complete") {
                                showCompleteSheet = true
                            }
                            Button {
                                var updated = draft
                                updated.status = .cancelled
                                draft = updated
                                store.updateSession(updated)
                            } label: {
                                Text("Cancel session")
                                    .font(.izadi(.bodyMedium))
                                    .foregroundStyle(IzadiColor.roseDeep)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 14)
                            }
                        } else {
                            Button {
                                var updated = draft
                                updated.status = .scheduled
                                updated.paymentStatus = .pending
                                updated.paymentAmountCents = nil
                                updated.paymentMethod = nil
                                draft = updated
                                store.updateSession(updated)
                            } label: {
                                Text("Restore to scheduled")
                                    .font(.izadi(.bodyMedium))
                                    .foregroundStyle(IzadiColor.sage)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 14)
                            }
                        }

                        if let completeError {
                            Text(completeError)
                                .font(.izadi(.bodyMedium))
                                .foregroundStyle(IzadiColor.roseDeep)
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 36)
                }
            }
            .padding(.top, 8)
        }
        .toolbar(.hidden, for: .navigationBar)
        .sheet(isPresented: $showCompleteSheet) {
            completeSheet
                .presentationDetents([.medium])
        }
        .onChange(of: store.sessions) { _, sessions in
            if let latest = sessions.first(where: { $0.id == draft.id }) {
                draft = latest
            }
        }
    }

    private var notesField: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("NOTES")
                .font(.izadi(.label))
                .tracking(2.2)
                .foregroundStyle(IzadiColor.sageSoft)
            TextField("Session notes", text: Binding(
                get: { draft.notes },
                set: { newValue in
                    draft.notes = newValue
                    store.updateSession(draft)
                }
            ), axis: .vertical)
            .font(.izadi(.body))
            .lineLimit(3...8)
            .padding(16)
            .background(IzadiColor.foam)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
    }

    private var completeSheet: some View {
        SoftScreenBackground(fullMint: true) {
            VStack(alignment: .leading, spacing: 16) {
                Text("Complete session")
                    .font(.izadi(.title))
                    .foregroundStyle(IzadiColor.ink)
                Text("Record payment to finish wrap-up.")
                    .font(.izadi(.body))
                    .foregroundStyle(IzadiColor.inkSoft)

                FoamField(title: "Amount (AUD)", text: $paymentDollars, keyboard: .decimalPad)

                PrimaryButton(title: "Paid cash") {
                    complete(method: .cash)
                }

                Button {
                    complete(method: .tapped)
                } label: {
                    Text(SquareConfig.isConfigured ? "Tap with Square" : "Tap (configure Square)")
                        .font(.izadi(.boldBody))
                        .foregroundStyle(IzadiColor.ink)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(IzadiColor.foam)
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                }
                .buttonStyle(.plain)
                .disabled(!SquareConfig.isConfigured)

                Text("Square Point of Sale must be installed and registered for this bundle ID. Cash always works offline.")
                    .font(.izadi(.bodyMedium))
                    .foregroundStyle(IzadiColor.inkSoft)

                Spacer()
            }
            .padding(24)
        }
    }

    private func complete(method: PaymentMethod) {
        guard let cents = MoneyFormatting.parseDollarsToCents(paymentDollars) else {
            completeError = "Enter a valid amount like 150 or 150.00"
            return
        }
        var updated = draft
        updated.status = .completed
        updated.paymentStatus = .paid
        updated.paymentAmountCents = cents
        updated.paymentMethod = method
        draft = updated
        store.updateSession(updated)
        showCompleteSheet = false
        completeError = nil
    }
}
