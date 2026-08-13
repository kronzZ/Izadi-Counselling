import SwiftUI
import MessageUI

struct CourtesySmsSheet: View {
    let session: Session
    let client: Client?
    let template: String
    let onTemplateChange: (String) -> Void
    let onMarkComplete: () -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var currentTemplate: String
    @State private var showTemplateEditor = false
    @State private var showComposer = false
    @State private var validationMessage: String?

    init(
        session: Session,
        client: Client?,
        template: String,
        onTemplateChange: @escaping (String) -> Void,
        onMarkComplete: @escaping () -> Void
    ) {
        self.session = session
        self.client = client
        self.template = template
        self.onTemplateChange = onTemplateChange
        self.onMarkComplete = onMarkComplete
        _currentTemplate = State(initialValue: template)
    }

    private var firstName: String {
        client?.firstName ?? session.clientName.split(separator: " ").first.map(String.init) ?? session.clientName
    }

    private var mobile: String {
        client?.mobile ?? ""
    }

    private var previewBody: String {
        CourtesySms.render(
            template: currentTemplate,
            firstName: firstName,
            sessionStartsAt: session.startsAt
        )
    }

    var body: some View {
        SoftScreenBackground(fullMint: true) {
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    Text("Courtesy SMS")
                        .font(.izadi(.title))
                        .foregroundStyle(IzadiColor.ink)
                    Spacer()
                    Button("Close") { dismiss() }
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.inkSoft)
                }

                Text("\(session.clientName) · \(session.shortDate) · \(session.friendlyTime)")
                    .font(.izadi(.bodyMedium))
                    .foregroundStyle(IzadiColor.inkSoft)

                Text(previewBody)
                    .font(.izadi(.body))
                    .foregroundStyle(IzadiColor.ink)
                    .padding(14)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(IzadiColor.foam)
                    .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))

                if let validationMessage {
                    Text(validationMessage)
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.roseDeep)
                }

                PrimaryButton(title: "Open SMS") {
                    guard CourtesySms.isValidMobile(mobile) else {
                        validationMessage = "This client needs a mobile number with at least 8 digits."
                        return
                    }
                    validationMessage = nil
                    if MFMessageComposeViewController.canSendText() {
                        showComposer = true
                    } else {
                        validationMessage = "Messaging isn’t available on this device (try a real iPhone)."
                    }
                }

                Button {
                    onMarkComplete()
                    dismiss()
                } label: {
                    Text("Mark as complete")
                        .font(.izadi(.boldBody))
                        .foregroundStyle(IzadiColor.ink)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(IzadiColor.foam)
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                }
                .buttonStyle(.plain)

                Button {
                    showTemplateEditor = true
                } label: {
                    Text("Edit template")
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.sage)
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.plain)
            }
            .padding(24)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        }
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
        .sheet(isPresented: $showTemplateEditor) {
            CourtesySmsTemplateEditor(
                template: currentTemplate,
                onCancel: { showTemplateEditor = false },
                onSave: { updated in
                    currentTemplate = updated
                    onTemplateChange(updated)
                    showTemplateEditor = false
                }
            )
            .presentationDetents([.large])
            .presentationDragIndicator(.visible)
        }
        .sheet(isPresented: $showComposer) {
            MessageComposeView(
                recipients: [mobile],
                body: previewBody
            )
        }
    }
}

private struct CourtesySmsTemplateEditor: View {
    @State private var draft: String
    let onCancel: () -> Void
    let onSave: (String) -> Void

    init(template: String, onCancel: @escaping () -> Void, onSave: @escaping (String) -> Void) {
        _draft = State(initialValue: template)
        self.onCancel = onCancel
        self.onSave = onSave
    }

    var body: some View {
        SoftScreenBackground(fullMint: true) {
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    Text("Edit courtesy template")
                        .font(.izadi(.title))
                        .foregroundStyle(IzadiColor.ink)
                    Spacer()
                    Button("Cancel", action: onCancel)
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.inkSoft)
                }

                Text("Use \(CourtesySms.firstNameToken) for their name and \(CourtesySms.whenToken) for today / tomorrow / weekday at time.")
                    .font(.izadi(.body))
                    .foregroundStyle(IzadiColor.inkSoft)

                TextEditor(text: $draft)
                    .font(.izadi(.bodyMedium))
                    .foregroundStyle(IzadiColor.ink)
                    .scrollContentBackground(.hidden)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .padding(12)
                    .background(IzadiColor.foam)
                    .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))

                PrimaryButton(title: "Save template") {
                    onSave(draft)
                }
            }
            .padding(24)
        }
    }
}
