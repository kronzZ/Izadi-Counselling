import SwiftUI

struct NewClientView: View {
    @EnvironmentObject private var store: PracticeStore
    @Binding var path: NavigationPath

    @State private var firstName = ""
    @State private var surname = ""
    @State private var dateOfBirth = ""
    @State private var mobile = ""
    @State private var emergencyContactName = ""
    @State private var emergencyContactNumber = ""
    @State private var relationship: EmergencyRelationship?
    @State private var createdClient: Client?
    @State private var showBookPrompt = false

    private var canSave: Bool {
        !firstName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            && !surname.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    var body: some View {
        SoftScreenBackground {
            VStack(alignment: .leading, spacing: 0) {
                BackButton { path.removeLast() }
                    .padding(.horizontal, 16)

                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        Text("New client")
                            .font(.izadi(.title))
                            .foregroundStyle(IzadiColor.ink)
                        Text("Add their details. You can book a session for them later.")
                            .font(.izadi(.body))
                            .foregroundStyle(IzadiColor.inkSoft)

                        FoamField(title: "First name", text: $firstName, autocapitalization: .words)
                        FoamField(title: "Surname", text: $surname, autocapitalization: .words)
                        FoamField(
                            title: "Date of birth",
                            text: $dateOfBirth,
                            keyboard: .numberPad,
                            onChange: DateOfBirthFormatting.format
                        )
                        FoamField(title: "Mobile", text: $mobile, keyboard: .phonePad)

                        FoamField(title: "Emergency contact", text: $emergencyContactName, autocapitalization: .words)
                        FoamField(title: "Emergency number", text: $emergencyContactNumber, keyboard: .phonePad)

                        relationshipPicker

                        PrimaryButton(title: "Save client", enabled: canSave, action: save)
                            .padding(.top, 8)
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 36)
                }
            }
            .padding(.top, 8)
        }
        .toolbar(.hidden, for: .navigationBar)
        .alert("Book first session?", isPresented: $showBookPrompt) {
            Button("Book session") {
                if let createdClient {
                    path.removeLast()
                    path.append(AppRoute.bookSession(createdClient.id))
                }
            }
            Button("Not now", role: .cancel) {
                path.removeLast()
                path.append(AppRoute.clients)
            }
        } message: {
            Text("Would you like to book their first session now?")
        }
    }

    private var relationshipPicker: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("EMERGENCY CONTACT RELATIONSHIP")
                .font(.izadi(.label))
                .tracking(2.2)
                .foregroundStyle(IzadiColor.sageSoft)

            Menu {
                Button("None") { relationship = nil }
                ForEach(EmergencyRelationship.allCases) { item in
                    Button(item.label) { relationship = item }
                }
            } label: {
                HStack {
                    Text(relationship?.label ?? "Select")
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

    private func save() {
        let client = Client(
            firstName: firstName.trimmingCharacters(in: .whitespacesAndNewlines),
            surname: surname.trimmingCharacters(in: .whitespacesAndNewlines),
            dateOfBirth: dateOfBirth,
            mobile: mobile.trimmingCharacters(in: .whitespacesAndNewlines),
            emergencyContactName: emergencyContactName.trimmingCharacters(in: .whitespacesAndNewlines),
            emergencyContactNumber: emergencyContactNumber.trimmingCharacters(in: .whitespacesAndNewlines),
            relationship: relationship,
            isActive: true
        )
        store.addClient(client)
        createdClient = client
        showBookPrompt = true
    }
}
