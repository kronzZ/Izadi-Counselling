import SwiftUI
import MessageUI

struct WelcomeSmsSheet: View {
    let template: String
    let onTemplateChange: (String) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var firstName = ""
    @State private var mobile = ""
    @State private var editableTemplate: String
    @State private var showComposer = false
    @State private var validationMessage: String?

    init(template: String, onTemplateChange: @escaping (String) -> Void) {
        self.template = template
        self.onTemplateChange = onTemplateChange
        _editableTemplate = State(initialValue: template)
    }

    var body: some View {
        SoftScreenBackground(fullMint: true) {
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    Text("Welcome SMS")
                        .font(.izadi(.title))
                        .foregroundStyle(IzadiColor.ink)
                    Spacer()
                    Button("Done") {
                        onTemplateChange(editableTemplate)
                        dismiss()
                    }
                    .font(.izadi(.bodyMedium))
                    .foregroundStyle(IzadiColor.sage)
                }

                Text("Opens Messages with your template. Nothing is sent until you tap Send.")
                    .font(.izadi(.body))
                    .foregroundStyle(IzadiColor.inkSoft)

                FoamField(title: "First name", text: $firstName, autocapitalization: .words)
                FoamField(title: "Mobile", text: $mobile, keyboard: .phonePad)

                VStack(alignment: .leading, spacing: 8) {
                    Text("TEMPLATE")
                        .font(.izadi(.label))
                        .tracking(2.2)
                        .foregroundStyle(IzadiColor.sageSoft)
                    TextEditor(text: $editableTemplate)
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.ink)
                        .scrollContentBackground(.hidden)
                        .frame(minHeight: 160)
                        .padding(12)
                        .background(IzadiColor.foam)
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                }

                if let validationMessage {
                    Text(validationMessage)
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.roseDeep)
                }

                PrimaryButton(title: "Open Messages") {
                    onTemplateChange(editableTemplate)
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
                        validationMessage = "Messaging isn’t available on this device."
                    }
                }

                Spacer()
            }
            .padding(24)
        }
        .sheet(isPresented: $showComposer) {
            MessageComposeView(
                recipients: [mobile],
                body: WelcomeSms.render(template: editableTemplate, firstName: firstName)
            )
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
