import SwiftUI
import UIKit

struct BackButton: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: "chevron.left")
                .font(.system(size: 18, weight: .medium))
                .foregroundStyle(IzadiColor.sage)
                .frame(width: 44, height: 44, alignment: .leading)
        }
        .buttonStyle(.plain)
    }
}

/// Disables the edge-swipe back gesture when leaving would discard unsaved edits.
struct InteractivePopDisabled: UIViewControllerRepresentable {
    var disabled: Bool

    func makeUIViewController(context: Context) -> Controller {
        Controller()
    }

    func updateUIViewController(_ uiViewController: Controller, context: Context) {
        uiViewController.disabled = disabled
        uiViewController.apply()
    }

    final class Controller: UIViewController {
        var disabled = false

        override func viewDidAppear(_ animated: Bool) {
            super.viewDidAppear(animated)
            apply()
        }

        func apply() {
            navigationController?.interactivePopGestureRecognizer?.isEnabled = !disabled
        }
    }
}

struct CircularAddButton: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: "plus")
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(.white)
                .frame(width: 32, height: 32)
                .background(IzadiColor.sage)
                .clipShape(Circle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel("New")
    }
}

struct PrimaryButton: View {
    let title: String
    var enabled: Bool = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.izadi(.boldBody))
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(enabled ? IzadiColor.sage : IzadiColor.sage.opacity(0.35))
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .disabled(!enabled)
        .buttonStyle(.plain)
    }
}

struct FoamField: View {
    let title: String
    @Binding var text: String
    var keyboard: UIKeyboardType = .default
    var autocapitalization: TextInputAutocapitalization = .sentences
    var isEditable: Bool = true
    var onChange: ((String) -> String)? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title.uppercased())
                .font(.izadi(.label))
                .tracking(2.2)
                .foregroundStyle(IzadiColor.sageSoft)

            Group {
                if isEditable {
                    TextField("", text: $text)
                        .font(.izadi(.body))
                        .foregroundStyle(IzadiColor.ink)
                        .keyboardType(keyboard)
                        .textInputAutocapitalization(autocapitalization)
                        .onChange(of: text) { _, newValue in
                            if let onChange {
                                let formatted = onChange(newValue)
                                if formatted != newValue {
                                    text = formatted
                                }
                            }
                        }
                } else {
                    Text(displayText)
                        .font(.izadi(.body))
                        .foregroundStyle(IzadiColor.ink)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
            .padding(16)
            .background(IzadiColor.foam)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
    }

    private var displayText: String {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.isEmpty ? "—" : text
    }
}

struct EmergencyRelationshipPicker: View {
    @Binding var relationship: EmergencyRelationship?
    @Binding var relationshipCustom: String
    var isEditable: Bool = true

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("EMERGENCY CONTACT RELATIONSHIP")
                .font(.izadi(.label))
                .tracking(2.2)
                .foregroundStyle(IzadiColor.sageSoft)

            Group {
                if isEditable {
                    Menu {
                        Button("None") {
                            relationship = nil
                            relationshipCustom = ""
                        }
                        ForEach(EmergencyRelationship.allCases) { item in
                            Button(item.label) {
                                relationship = item
                                if item != .other {
                                    relationshipCustom = ""
                                }
                            }
                        }
                    } label: {
                        pickerRow(showChevron: true)
                    }
                } else {
                    pickerRow(showChevron: false)
                }
            }

            if isEditable && relationship == .other {
                FoamField(
                    title: "Specify relationship",
                    text: $relationshipCustom,
                    autocapitalization: .words
                )
            } else if !isEditable && relationship == .other && !relationshipCustom.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                FoamField(
                    title: "Specify relationship",
                    text: $relationshipCustom,
                    isEditable: false
                )
            }
        }
    }

    private func pickerRow(showChevron: Bool) -> some View {
        HStack {
            Text(pickerLabel)
                .font(.izadi(.body))
                .foregroundStyle(IzadiColor.ink)
            Spacer()
            if showChevron {
                Image(systemName: "chevron.up.chevron.down")
                    .foregroundStyle(IzadiColor.sageSoft)
            }
        }
        .padding(16)
        .background(IzadiColor.foam)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
    }

    private var pickerLabel: String {
        guard let relationship else { return "Select" }
        if relationship == .other {
            let trimmed = relationshipCustom.trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty ? relationship.label : trimmed
        }
        return relationship.label
    }
}

struct SegmentedTabs: View {
    let tabs: [String]
    @Binding var selected: Int

    var body: some View {
        HStack(spacing: 8) {
            ForEach(tabs.indices, id: \.self) { index in
                Button {
                    selected = index
                } label: {
                    Text(tabs[index])
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(selected == index ? IzadiColor.ink : IzadiColor.inkSoft)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(selected == index ? IzadiColor.foam : Color.clear)
                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                }
                .buttonStyle(.plain)
            }
        }
        .padding(4)
        .background(IzadiColor.bloom)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
    }
}

struct SessionRowCard: View {
    let session: Session
    var awaitingWrapUp: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(alignment: .center, spacing: 10) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(session.clientName)
                        .font(.izadi(.body))
                        .foregroundStyle(IzadiColor.ink)
                    Text("\(session.shortDate) · \(session.friendlyTime)")
                        .font(.izadi(.label))
                        .tracking(0)
                        .foregroundStyle(IzadiColor.inkSoft)
                    if awaitingWrapUp {
                        Text("Awaiting wrap-up")
                            .font(.izadi(.label))
                            .tracking(1.2)
                            .foregroundStyle(IzadiColor.roseDeep)
                    }
                }
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(IzadiColor.sageSoft)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(awaitingWrapUp ? IzadiColor.butter : IzadiColor.foam)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}

struct EmptyStateText: View {
    let title: String
    let subtitle: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.izadi(.titleMedium))
                .foregroundStyle(IzadiColor.ink)
            Text(subtitle)
                .font(.izadi(.body))
                .foregroundStyle(IzadiColor.inkSoft)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.vertical, 24)
    }
}
