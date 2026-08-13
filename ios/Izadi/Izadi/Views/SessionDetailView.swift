import SwiftUI

struct SessionDetailView: View {
    @EnvironmentObject private var store: PracticeStore
    @EnvironmentObject private var squarePayments: SquarePaymentCoordinator
    @EnvironmentObject private var appLock: AppLockService
    let session: Session
    @Binding var path: NavigationPath

    @State private var draft: Session
    @State private var isEditing = false
    @State private var editBaseline: Session?
    @State private var showCompleteSheet = false
    @State private var paymentDollars = "150"
    @State private var completeError: String?
    /// Kept across the Square app switch so we still know the amount on return.
    @State private var pendingTapAmountCents: Int?
    @State private var showDeletePaymentConfirm = false
    @State private var showDiscardConfirm = false

    init(session: Session, path: Binding<NavigationPath>) {
        self.session = session
        self._path = path
        self._draft = State(initialValue: session)
    }

    private var hasUnsavedScheduleChanges: Bool {
        guard isEditing, let editBaseline else { return false }
        return draft.date != editBaseline.date || draft.timeMinutes != editBaseline.timeMinutes
    }

    private var rollingDates: [Date] {
        let calendar = Calendar.current
        let start = calendar.startOfDay(for: Date())
        var dates = (0..<61).compactMap { calendar.date(byAdding: .day, value: $0, to: start) }
        let sessionDay = calendar.startOfDay(for: draft.date)
        if !dates.contains(where: { calendar.isDate($0, inSameDayAs: sessionDay) }) {
            dates.append(sessionDay)
            dates.sort()
        }
        return dates
    }

    var body: some View {
        SoftScreenBackground {
            VStack(alignment: .leading, spacing: 0) {
                HStack {
                    BackButton { attemptLeave() }
                    Spacer()
                    if draft.status == .scheduled && !isEditing {
                        Button("Edit") {
                            startEditing()
                        }
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.sage)
                        .padding(.trailing, 20)
                    }
                }
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

                        if isEditing {
                            scheduleEditor
                        } else {
                            Text(draft.friendlyDate)
                                .font(.izadi(.body))
                                .foregroundStyle(IzadiColor.ink)
                            Text("\(draft.friendlyTime) · \(draft.durationMinutes) minutes")
                                .font(.izadi(.bodyMedium))
                                .foregroundStyle(IzadiColor.inkSoft)
                        }

                        if let paid = draft.formattedPaidSummary {
                            Text(paid)
                                .font(.izadi(.bodyMedium))
                                .foregroundStyle(IzadiColor.sage)
                        }

                        if !isEditing {
                            notesField
                        }

                        if isEditing {
                            PrimaryButton(title: "Save changes", enabled: hasUnsavedScheduleChanges) {
                                saveScheduleEdits()
                            }

                            Button("Cancel") {
                                discardEdits()
                            }
                            .font(.izadi(.bodyMedium))
                            .foregroundStyle(IzadiColor.sage)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 8)
                        } else if draft.status == .scheduled {
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

                        if !isEditing, draft.paymentStatus == .paid {
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
        .background(InteractivePopDisabled(disabled: hasUnsavedScheduleChanges))
        .sheet(isPresented: $showCompleteSheet) {
            completeSheet
                .presentationDetents([.medium])
        }
        .alert("Discard schedule changes?", isPresented: $showDiscardConfirm) {
            Button("Discard", role: .destructive) {
                discardEditsAndLeave()
            }
            Button("Keep editing", role: .cancel) {}
        } message: {
            Text("Your date and time changes won’t be saved.")
        }
        .onChange(of: store.sessions) { _, sessions in
            guard !isEditing else { return }
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

    private var scheduleEditor: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("DATE")
                .font(.izadi(.label))
                .tracking(2.2)
                .foregroundStyle(IzadiColor.sageSoft)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(rollingDates, id: \.self) { date in
                        let selected = Calendar.current.isDate(date, inSameDayAs: draft.date)
                        Button {
                            draft.date = Calendar.current.startOfDay(for: date)
                        } label: {
                            VStack(spacing: 4) {
                                Text(date.formatted(.dateTime.weekday(.abbreviated)))
                                    .font(.izadi(.label))
                                Text("\(Calendar.current.component(.day, from: date))")
                                    .font(.izadi(.titleMedium))
                            }
                            .foregroundStyle(selected ? .white : IzadiColor.ink)
                            .frame(width: 64, height: 72)
                            .background(selected ? IzadiColor.sage : IzadiColor.foam)
                            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                        }
                        .buttonStyle(.plain)
                    }
                }
            }

            Text("TIME")
                .font(.izadi(.label))
                .tracking(2.2)
                .foregroundStyle(IzadiColor.sageSoft)

            HStack(spacing: 16) {
                timeStepper(title: "−") { adjustTime(-5) }
                Text(timeLabel)
                    .font(.izadi(.titleMedium))
                    .foregroundStyle(IzadiColor.ink)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(IzadiColor.foam)
                    .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                timeStepper(title: "+") { adjustTime(5) }
            }

            Text("Duration stays \(draft.durationMinutes) minutes.")
                .font(.izadi(.bodyMedium))
                .foregroundStyle(IzadiColor.inkSoft)
        }
    }

    private var timeLabel: String {
        let hour = draft.timeMinutes / 60
        let minute = draft.timeMinutes % 60
        var components = DateComponents()
        components.hour = hour
        components.minute = minute
        let date = Calendar.current.date(from: components) ?? Date()
        return date.formatted(.dateTime.hour().minute())
    }

    private func adjustTime(_ delta: Int) {
        draft.timeMinutes = min(max(draft.timeMinutes + delta, 0), (23 * 60) + 55)
    }

    private func timeStepper(title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.izadi(.title))
                .foregroundStyle(IzadiColor.sage)
                .frame(width: 52, height: 52)
                .background(IzadiColor.foam)
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .buttonStyle(.plain)
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

    private func startEditing() {
        editBaseline = draft
        isEditing = true
    }

    private func discardEdits() {
        if let editBaseline {
            draft = editBaseline
        }
        isEditing = false
        editBaseline = nil
    }

    private func discardEditsAndLeave() {
        discardEdits()
        path.removeLast()
    }

    private func attemptLeave() {
        if hasUnsavedScheduleChanges {
            showDiscardConfirm = true
        } else if isEditing {
            discardEdits()
            path.removeLast()
        } else {
            path.removeLast()
        }
    }

    private func saveScheduleEdits() {
        guard let editBaseline else { return }
        let scheduleChanged =
            draft.date != editBaseline.date || draft.timeMinutes != editBaseline.timeMinutes

        var updated = draft
        if scheduleChanged {
            // Reschedule courtesy reminders against the new slot.
            updated.courtesySmsCompleted = false
        }
        draft = updated
        store.updateSession(updated)
        isEditing = false
        self.editBaseline = nil
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
