import SwiftUI

struct SessionDetailView: View {
    @EnvironmentObject private var store: PracticeStore
    @EnvironmentObject private var squarePayments: SquarePaymentCoordinator
    @EnvironmentObject private var appLock: AppLockService
    let session: Session
    @Binding var path: NavigationPath

    @State private var draft: Session
    @State private var showCompleteSheet = false
    @State private var paymentDollars = "150"
    @State private var completeError: String?
    /// Kept across the Square app switch so we still know the amount on return.
    @State private var pendingTapAmountCents: Int?
    @State private var showDeletePaymentConfirm = false

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
                                completeError = nil
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

                        if draft.paymentStatus == .paid {
                            Button {
                                showDeletePaymentConfirm = true
                            } label: {
                                Text("Delete payment")
                                    .font(.izadi(.boldBody))
                                    .foregroundStyle(.white)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 16)
                                    .background(IzadiColor.roseDeep)
                                    .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                            }
                            .buttonStyle(.plain)
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
        .alert("Delete this payment?", isPresented: $showDeletePaymentConfirm) {
            Button("Delete", role: .destructive) {
                store.deleteSession(sessionId: draft.id)
                path.removeLast()
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("All data for this payment record will be lost, including the session details. This can’t be undone.")
        }
        .onChange(of: store.sessions) { _, sessions in
            if let latest = sessions.first(where: { $0.id == draft.id }) {
                draft = latest
            }
        }
        .onChange(of: squarePayments.completedSessionId) { _, sessionId in
            guard let sessionId, sessionId == draft.id else { return }
            let cents = pendingTapAmountCents
                ?? MoneyFormatting.parseDollarsToCents(paymentDollars)
            squarePayments.clearCompletion()
            guard let cents else {
                completeError = "Payment succeeded in Square, but the amount could not be recorded. Mark cash manually if needed."
                return
            }
            applyPaid(amountCents: cents, method: .tapped)
            pendingTapAmountCents = nil
        }
        .onChange(of: squarePayments.lastError) { _, message in
            guard let message else { return }
            completeError = message
            pendingTapAmountCents = nil
            squarePayments.clearError()
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
                    completeCash()
                }

                Button {
                    startSquareTap()
                } label: {
                    Text("Tap with Square")
                        .font(.izadi(.boldBody))
                        .foregroundStyle(IzadiColor.ink)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(IzadiColor.foam)
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                }
                .buttonStyle(.plain)
                .disabled(!SquareConfig.isConfigured)

                Text("Opens Square Point of Sale on this phone. The session is marked paid only after Square confirms the payment.")
                    .font(.izadi(.bodyMedium))
                    .foregroundStyle(IzadiColor.inkSoft)

                if let completeError {
                    Text(completeError)
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.roseDeep)
                }

                Spacer()
            }
            .padding(24)
        }
    }

    private func completeCash() {
        guard let cents = MoneyFormatting.parseDollarsToCents(paymentDollars) else {
            completeError = "Enter a valid amount like 150 or 150.00"
            return
        }
        applyPaid(amountCents: cents, method: .cash)
    }

    private func startSquareTap() {
        guard let cents = MoneyFormatting.parseDollarsToCents(paymentDollars) else {
            completeError = "Enter a valid amount like 150 or 150.00"
            return
        }
        completeError = nil
        pendingTapAmountCents = cents

        do {
            // Leaving for Square POS — don't demand Face ID on the way back.
            appLock.allowNextResumeWithoutAuth()
            try squarePayments.startTapPayment(
                sessionId: draft.id,
                amountCents: cents,
                clientName: draft.clientName
            )
            showCompleteSheet = false
        } catch SquarePaymentError.squareNotInstalled {
            completeError = SquarePaymentError.squareNotInstalled.localizedDescription
            squarePayments.openAppStoreListing()
        } catch {
            completeError = error.localizedDescription
        }
    }

    private func applyPaid(amountCents: Int, method: PaymentMethod) {
        var updated = draft
        updated.status = .completed
        updated.paymentStatus = .paid
        updated.paymentAmountCents = amountCents
        updated.paymentMethod = method
        draft = updated
        store.updateSession(updated)
        showCompleteSheet = false
        completeError = nil
    }
}
