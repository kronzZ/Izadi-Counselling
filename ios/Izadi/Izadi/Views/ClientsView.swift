import SwiftUI

struct ClientsView: View {
    @EnvironmentObject private var store: PracticeStore
    @Binding var path: NavigationPath
    var pickerMode: Bool = false

    @State private var tab = 0
    @State private var query = ""
    @State private var sortNewestFirst = true

    var body: some View {
        SoftScreenBackground {
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    BackButton { path.removeLast() }
                    Spacer()
                    if !pickerMode {
                        Button {
                            path.append(AppRoute.newClient)
                        } label: {
                            Text("New")
                                .font(.izadi(.bodyMedium))
                                .foregroundStyle(IzadiColor.sage)
                        }
                    }
                }
                .padding(.horizontal, 16)

                VStack(alignment: .leading, spacing: 16) {
                    Text(pickerMode ? "Choose client" : "Clients")
                        .font(.izadi(.title))
                        .foregroundStyle(IzadiColor.ink)

                    SegmentedTabs(tabs: ["Active", "Inactive"], selected: $tab)

                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundStyle(IzadiColor.sageSoft)
                        TextField("Search name or mobile", text: $query)
                            .font(.izadi(.bodyMedium))
                    }
                    .padding(14)
                    .background(IzadiColor.foam)
                    .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))

                    Button {
                        sortNewestFirst.toggle()
                    } label: {
                        Text(sortNewestFirst ? "Newest first" : "Name A–Z")
                            .font(.izadi(.label))
                            .tracking(1.6)
                            .foregroundStyle(IzadiColor.sage)
                    }

                    ScrollView {
                        LazyVStack(spacing: 7) {
                            ForEach(filteredClients) { client in
                                Button {
                                    if pickerMode {
                                        path.append(AppRoute.bookSession(client.id))
                                    } else {
                                        path.append(AppRoute.clientDetail(client.id))
                                    }
                                } label: {
                                    HStack(alignment: .center, spacing: 10) {
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(client.fullName)
                                                .font(.izadi(.body))
                                                .foregroundStyle(IzadiColor.ink)
                                            Text(client.mobile.isEmpty ? "No mobile" : client.mobile)
                                                .font(.izadi(.label))
                                                .tracking(0)
                                                .foregroundStyle(IzadiColor.inkSoft)
                                        }
                                        Spacer()
                                        Image(systemName: "chevron.right")
                                            .font(.system(size: 12, weight: .semibold))
                                            .foregroundStyle(IzadiColor.sageSoft)
                                    }
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 10)
                                    .background(
                                        store.highlightClientId == client.id
                                        ? IzadiColor.bloom
                                        : IzadiColor.foam
                                    )
                                    .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                                }
                                .buttonStyle(.plain)
                                .onAppear {
                                    if store.highlightClientId == client.id {
                                        DispatchQueue.main.asyncAfter(deadline: .now() + 1.6) {
                                            store.clearHighlightClientId()
                                        }
                                    }
                                }
                            }
                        }
                        .padding(.bottom, 28)
                    }
                }
                .padding(.horizontal, 24)
            }
            .padding(.top, 8)
        }
        .toolbar(.hidden, for: .navigationBar)
    }

    private var filteredClients: [Client] {
        let active = tab == 0
        var list = store.clients.filter { $0.isActive == active }
        let q = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if !q.isEmpty {
            list = list.filter {
                $0.fullName.lowercased().contains(q) || $0.mobile.lowercased().contains(q)
            }
        }
        if sortNewestFirst {
            return list.sorted { $0.createdAtEpochMs > $1.createdAtEpochMs }
        }
        return list.sorted {
            $0.fullName.localizedCaseInsensitiveCompare($1.fullName) == .orderedAscending
        }
    }
}
