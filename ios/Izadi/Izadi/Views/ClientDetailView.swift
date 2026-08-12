import SwiftUI

struct ClientDetailView: View {
    @EnvironmentObject private var store: PracticeStore
    let client: Client
    @Binding var path: NavigationPath

    @State private var draft: Client
    @State private var isEditing = false
    @State private var editBaseline: Client?
    @State private var showDeleteConfirm = false
    @State private var showDiscardConfirm = false

    init(client: Client, path: Binding<NavigationPath>) {
        self.client = client
        self._path = path
        self._draft = State(initialValue: client)
    }

    private var savedClient: Client {
        store.clients.first(where: { $0.id == client.id }) ?? client
    }

    private var hasUnsavedChanges: Bool {
        guard isEditing, let editBaseline else { return false }
        return draft != editBaseline
    }

    var body: some View {
        SoftScreenBackground {
            VStack(alignment: .leading, spacing: 0) {
                HStack {
                    BackButton { attemptLeave() }
                    Spacer()
                    if !isEditing {
                        Button(savedClient.isActive ? "Set inactive" : "Set active") {
                            store.toggleClientActive(clientId: savedClient.id)
                        }
                        .font(.izadi(.bodyMedium))
                        .foregroundStyle(IzadiColor.sage)

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
                        Text(isEditing ? draft.fullName : savedClient.fullName)
                            .font(.izadi(.title))
                            .foregroundStyle(IzadiColor.ink)
                        Text((isEditing ? draft.isActive : savedClient.isActive) ? "Active client" : "Inactive client")
                            .font(.izadi(.label))
                            .tracking(2.2)
                            .foregroundStyle(IzadiColor.sageSoft)

                        FoamField(
                            title: "First name",
                            text: binding(\.firstName),
                            autocapitalization: .words,
                            isEditable: isEditing
                        )
                        FoamField(
                            title: "Surname",
                            text: binding(\.surname),
                            autocapitalization: .words,
                            isEditable: isEditing
                        )
                        FoamField(
                            title: "Date of birth",
                            text: binding(\.dateOfBirth),
                            keyboard: .numberPad,
                            isEditable: isEditing,
                            onChange: DateOfBirthFormatting.format
                        )
                        FoamField(
                            title: "Mobile",
                            text: binding(\.mobile),
                            keyboard: .phonePad,
                            isEditable: isEditing
                        )
                        FoamField(
                            title: "Emergency contact",
                            text: binding(\.emergencyContactName),
                            autocapitalization: .words,
                            isEditable: isEditing
                        )
                        FoamField(
                            title: "Emergency number",
                            text: binding(\.emergencyContactNumber),
                            keyboard: .phonePad,
                            isEditable: isEditing
                        )

                        EmergencyRelationshipPicker(
                            relationship: binding(\.relationship),
                            relationshipCustom: binding(\.relationshipCustom),
                            isEditable: isEditing
                        )

                        PrimaryButton(title: "View sessions") {
                            path.append(AppRoute.clientSessions(savedClient.id))
                        }

                        if isEditing {
                            PrimaryButton(title: "Save changes", action: saveChanges)
                                .padding(.top, 4)
                        }

                        if !isEditing {
                            Button {
                                showDeleteConfirm = true
                            } label: {
                                Text("Delete client")
                                    .font(.izadi(.boldBody))
                                    .foregroundStyle(.white)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 16)
                                    .background(IzadiColor.roseDeep)
                                    .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 36)
                }
            }
            .padding(.top, 8)
        }
        .toolbar(.hidden, for: .navigationBar)
        .background(InteractivePopDisabled(disabled: hasUnsavedChanges))
        .onChange(of: savedClient) { _, updated in
            if !isEditing {
                draft = updated
            }
        }
        .alert("Delete this client?", isPresented: $showDeleteConfirm) {
            Button("Delete", role: .destructive) {
                store.deleteClient(clientId: savedClient.id)
                path.removeLast()
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("All data for this client will be lost, including their sessions and payment history. This can’t be undone.")
        }
        .alert("Discard changes?", isPresented: $showDiscardConfirm) {
            Button("Discard changes", role: .destructive) {
                discardEditsAndLeave()
            }
            Button("Keep editing", role: .cancel) {}
        } message: {
            Text("You have unsaved changes to this client. If you leave now, those edits will be lost.")
        }
    }

    private func startEditing() {
        draft = savedClient
        editBaseline = savedClient
        isEditing = true
    }

    private func saveChanges() {
        store.updateClient(draft)
        isEditing = false
        editBaseline = nil
    }

    private func attemptLeave() {
        if hasUnsavedChanges {
            showDiscardConfirm = true
        } else {
            if isEditing {
                isEditing = false
                editBaseline = nil
                draft = savedClient
            }
            path.removeLast()
        }
    }

    private func discardEditsAndLeave() {
        isEditing = false
        editBaseline = nil
        draft = savedClient
        path.removeLast()
    }

    private func binding(_ keyPath: WritableKeyPath<Client, String>) -> Binding<String> {
        Binding(
            get: {
                if isEditing {
                    return draft[keyPath: keyPath]
                }
                return savedClient[keyPath: keyPath]
            },
            set: { draft[keyPath: keyPath] = $0 }
        )
    }

    private func binding(_ keyPath: WritableKeyPath<Client, EmergencyRelationship?>) -> Binding<EmergencyRelationship?> {
        Binding(
            get: {
                if isEditing {
                    return draft[keyPath: keyPath]
                }
                return savedClient[keyPath: keyPath]
            },
            set: { draft[keyPath: keyPath] = $0 }
        )
    }
}
