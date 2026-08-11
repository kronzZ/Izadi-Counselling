import SwiftUI
import MessageUI

struct WelcomeSmsSheet: View {
    let template: String
    let onTemplateChange: (String) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var firstName = ""
    @State private var mobile = ""
    @State private var currentTemplate: String
    @State private var showTemplateEditor = false
    @State private var showComposer = false
    @State private var validationMessage: String?

    init(template: String, onTemplateChange: @escaping (String) -> Void) {
        self.template = template
        self.onTemplateChange = onTemplateChange
        _currentTemplate = State(initialValue: template)
    }

    var body: some View {
        SoftScreenBackground(fullMint: true) {
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    Text("Send welcome SMS")
                        .font(.izadi(.title))
                        .foregroundStyle(IzadiColor.ink)
                    Spacer()
                    Button("Cancel") { dismiss() }
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.inkSoft)
                }

                Text("Enter their details and we’ll open Messages with the welcome note ready to send.")
                    .font(.izadi(.body))
                    .foregroundStyle(IzadiColor.inkSoft)

                FoamField(title: "First name", text: $firstName, autocapitalization: .words)
                FoamField(title: "Mobile", text: $mobile, keyboard: .phonePad)

                if let validationMessage {
                    Text(validationMessage)
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.roseDeep)
                }

                PrimaryButton(title: "Open SMS") {
                    guard !firstName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
                        validationMessage = "Add a first name."
                        return
                    }
                    guard WelcomeSms.isValidMobile(mobile) else {
                        validationMessage = "Mobile needs at least 8 digits."
                        return
                    }
                    validationMessage = nil
                    if MFMessageComposeViewController.canSendText() {
                        showComposer = true
                    } else {
                        // Simulator often can’t send SMS — still allow preview path messaging.
                        validationMessage = "Messaging isn’t available on this device (try a real iPhone)."
                    }
                }

                Button {
                    showTemplateEditor = true
                } label: {
                    Text("Edit template")
                        .font(.izadi(.boldBody))
                        .foregroundStyle(IzadiColor.ink)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(IzadiColor.foam)
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                }
                .buttonStyle(.plain)

                Spacer()
            }
            .padding(24)
        }
        .sheet(isPresented: $showTemplateEditor) {
            WelcomeSmsTemplateEditor(
                template: currentTemplate,
                onCancel: { showTemplateEditor = false },
                onSave: { updated in
                    currentTemplate = updated
                    onTemplateChange(updated)
                    showTemplateEditor = false
                }
            )
        }
        .sheet(isPresented: $showComposer) {
            MessageComposeView(
                recipients: [mobile],
                body: WelcomeSms.render(template: currentTemplate, firstName: firstName)
            )
        }
    }
}

private struct WelcomeSmsTemplateEditor: View {
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
                    Text("Edit welcome template")
                        .font(.izadi(.title))
                        .foregroundStyle(IzadiColor.ink)
                    Spacer()
                    Button("Cancel", action: onCancel)
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.inkSoft)
                }

                Text("Use \(WelcomeSms.firstNameToken) where their first name should appear.")
                    .font(.izadi(.body))
                    .foregroundStyle(IzadiColor.inkSoft)

                TextEditor(text: $draft)
                    .font(.izadi(.bodyMedium))
                    .foregroundStyle(IzadiColor.ink)
                    .scrollContentBackground(.hidden)
                    .frame(minHeight: 220)
                    .padding(12)
                    .background(IzadiColor.foam)
                    .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))

                PrimaryButton(title: "Save template") {
                    onSave(draft)
                }

                Spacer()
            }
            .padding(24)
        }
    }
}

struct MessageComposeView: UIViewControllerRepresentable {
    let recipients: [String]
    let body: String

    func makeCoordinator() -> Coordinator { Coordinator() }

    func makeUIViewController(context: Context) -> MFMessageComposeViewController {
        let controller = MFMessageComposeViewController()
        controller.messageComposeDelegate = context.coordinator
        controller.recipients = recipients
        controller.body = body
        return controller
    }

    func updateUIViewController(_ uiViewController: MFMessageComposeViewController, context: Context) {}

    final class Coordinator: NSObject, MFMessageComposeViewControllerDelegate {
        func messageComposeViewController(
            _ controller: MFMessageComposeViewController,
            didFinishWith result: MessageComposeResult
        ) {
            controller.dismiss(animated: true)
        }
    }
}
