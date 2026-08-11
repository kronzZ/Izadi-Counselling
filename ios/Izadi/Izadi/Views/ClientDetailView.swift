import SwiftUI

struct ClientDetailView: View {
    @EnvironmentObject private var store: PracticeStore
    let client: Client
    @Binding var path: NavigationPath

    @State private var draft: Client
    @State private var showDeleteConfirm = false

    init(client: Client, path: Binding<NavigationPath>) {
        self.client = client
        self._path = path
        self._draft = State(initialValue: client)
    }

    var body: some View {
        SoftScreenBackground {
            VStack(alignment: .leading, spacing: 0) {
                HStack {
                    BackButton { path.removeLast() }
                    Spacer()
                    Button(draft.isActive ? "Set inactive" : "Set active") {
                        store.toggleClientActive(clientId: draft.id)
                        draft.isActive.toggle()
                    }
                    .font(.izadi(.bodyMedium))
                    .foregroundStyle(IzadiColor.sage)
                    .padding(.trailing, 20)
                }
                .padding(.horizontal, 16)

                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        Text(draft.fullName)
                            .font(.izadi(.title))
                            .foregroundStyle(IzadiColor.ink)
                        Text(draft.isActive ? "Active client" : "Inactive client")
                            .font(.izadi(.label))
                            .tracking(2.2)
                            .foregroundStyle(IzadiColor.sageSoft)

                        FoamField(title: "First name", text: binding(\.firstName), autocapitalization: .words)
                        FoamField(title: "Surname", text: binding(\.surname), autocapitalization: .words)
                        FoamField(
                            title: "Date of birth",
                            text: binding(\.dateOfBirth),
                            keyboard: .numberPad,
                            onChange: DateOfBirthFormatting.format
                        )
                        FoamField(title: "Mobile", text: binding(\.mobile), keyboard: .phonePad)
                        FoamField(title: "Emergency contact", text: binding(\.emergencyContactName), autocapitalization: .words)
                        FoamField(title: "Emergency number", text: binding(\.emergencyContactNumber), keyboard: .phonePad)

                        relationshipPicker

                        PrimaryButton(title: "View sessions") {
                            path.append(AppRoute.clientSessions(draft.id))
                        }

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
                    .padding(.horizontal, 24)
                    .padding(.bottom, 36)
                }
            }
            .padding(.top, 8)
        }
        .toolbar(.hidden, for: .navigationBar)
        .onChange(of: draft) { _, newValue in
            store.updateClient(newValue)
        }
        .alert("Delete this client?", isPresented: $showDeleteConfirm) {
            Button("Delete", role: .destructive) {
                store.deleteClient(clientId: draft.id)
                path.removeLast()
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("All data for this client will be lost, including their sessions and payment history. This can’t be undone.")
        }
    }

    private var relationshipPicker: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("EMERGENCY CONTACT RELATIONSHIP")
                .font(.izadi(.label))
                .tracking(2.2)
                .foregroundStyle(IzadiColor.sageSoft)

            Menu {
                Button("None") { draft.relationship = nil }
                ForEach(EmergencyRelationship.allCases) { item in
                    Button(item.label) { draft.relationship = item }
                }
            } label: {
                HStack {
                    Text(draft.relationship?.label ?? "Select")
                        .font(.izadi(.body))
                        .foregroundStyle(IzadiColor.ink)
                    Spacer()
                    Image(systemName: "chevron.up.chevron.down")
                        .foregroundStyle(IzadiColor.sageSoft)
                }
                .padding(16)
                .background(IzadiColor.foam)
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            }
        }
    }

    private func binding(_ keyPath: WritableKeyPath<Client, String>) -> Binding<String> {
        Binding(
            get: { draft[keyPath: keyPath] },
            set: { draft[keyPath: keyPath] = $0 }
        )
    }
}
