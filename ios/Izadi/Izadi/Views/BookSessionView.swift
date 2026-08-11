import SwiftUI

struct BookSessionView: View {
    @EnvironmentObject private var store: PracticeStore
    let client: Client
    @Binding var path: NavigationPath

    @State private var selectedDate: Date = Calendar.current.startOfDay(for: Date())
    @State private var timeMinutes: Int = TimeHelpers.nextFiveMinuteSlot()
    @State private var durationMinutes = 50

    private var rollingDates: [Date] {
        let calendar = Calendar.current
        let start = calendar.startOfDay(for: Date())
        return (0..<61).compactMap { calendar.date(byAdding: .day, value: $0, to: start) }
    }

    var body: some View {
        SoftScreenBackground {
            VStack(alignment: .leading, spacing: 0) {
                BackButton { path.removeLast() }
                    .padding(.horizontal, 16)

                ScrollView {
                    VStack(alignment: .leading, spacing: 18) {
                        Text("Book session")
                            .font(.izadi(.title))
                            .foregroundStyle(IzadiColor.ink)
                        Text(client.fullName)
                            .font(.izadi(.body))
                            .foregroundStyle(IzadiColor.inkSoft)

                        Text("DATE")
                            .font(.izadi(.label))
                            .tracking(2.2)
                            .foregroundStyle(IzadiColor.sageSoft)

                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(rollingDates, id: \.self) { date in
                                    let selected = Calendar.current.isDate(date, inSameDayAs: selectedDate)
                                    Button {
                                        selectedDate = date
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
                            stepper(title: "−", action: { adjustTime(-5) })
                            Text(timeLabel)
                                .font(.izadi(.titleMedium))
                                .foregroundStyle(IzadiColor.ink)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                                .background(IzadiColor.foam)
                                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                            stepper(title: "+", action: { adjustTime(5) })
                        }

                        Text("DURATION")
                            .font(.izadi(.label))
                            .tracking(2.2)
                            .foregroundStyle(IzadiColor.sageSoft)

                        HStack(spacing: 8) {
                            ForEach(SessionQueries.durationOptions, id: \.self) { minutes in
                                Button {
                                    durationMinutes = minutes
                                } label: {
                                    Text("\(minutes)m")
                                        .font(.izadi(.bodyMedium))
                                        .foregroundStyle(durationMinutes == minutes ? .white : IzadiColor.ink)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 12)
                                        .background(durationMinutes == minutes ? IzadiColor.sage : IzadiColor.foam)
                                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                                }
                                .buttonStyle(.plain)
                            }
                        }

                        PrimaryButton(title: "Save booking") {
                            let session = Session(
                                clientId: client.id,
                                clientName: client.fullName,
                                date: selectedDate,
                                timeMinutes: timeMinutes,
                                durationMinutes: durationMinutes
                            )
                            store.addSession(session)
                            path = NavigationPath()
                        }
                        .padding(.top, 8)
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 36)
                }
            }
            .padding(.top, 8)
        }
        .toolbar(.hidden, for: .navigationBar)
    }

    private var timeLabel: String {
        let hour = timeMinutes / 60
        let minute = timeMinutes % 60
        var components = DateComponents()
        components.hour = hour
        components.minute = minute
        let date = Calendar.current.date(from: components) ?? Date()
        return date.formatted(.dateTime.hour().minute())
    }

    private func adjustTime(_ delta: Int) {
        timeMinutes = min(max(timeMinutes + delta, 0), (23 * 60) + 55)
    }

    private func stepper(title: String, action: @escaping () -> Void) -> some View {
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
}
