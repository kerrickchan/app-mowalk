import SwiftUI

struct CalendarView: View {
    @State private var viewModel: CalendarViewModel

    init(viewModel: CalendarViewModel) {
        self.viewModel = viewModel
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                BrandHeader(date: Date.todayISO)

                VStack(spacing: 12) {
                    HStack {
                        Button(action: { viewModel.previousMonth() }) {
                            HStack(spacing: 4) {
                                Image(systemName: "chevron.left")
                                Text(viewModel.previousMonthName)
                                    .lineLimit(1)
                                    .font(.system(size: 13))
                            }
                            .foregroundStyle(MoWalkTheme.textMuted)
                        }

                        Spacer()

                        Button(action: { viewModel.goToToday() }) {
                            Text(viewModel.monthName)
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundStyle(MoWalkTheme.textPrimary)
                        }

                        Spacer()

                        Button(action: { viewModel.nextMonth() }) {
                            HStack(spacing: 4) {
                                Text(viewModel.nextMonthName)
                                    .lineLimit(1)
                                    .font(.system(size: 13))
                                Image(systemName: "chevron.right")
                            }
                            .foregroundStyle(MoWalkTheme.textMuted)
                        }
                    }
                    .padding(.horizontal, 20)

                    HStack(spacing: 0) {
                        ForEach(["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"], id: \.self) { day in
                            Text(day)
                                .font(.system(size: 11, weight: .semibold))
                                .foregroundStyle(MoWalkTheme.textDim)
                                .textCase(.uppercase)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 6)
                        }
                    }
                    .padding(.horizontal, 16)

                    LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 2), count: 7), spacing: 2) {
                        ForEach(viewModel.days) { day in
                            Button {
                                viewModel.selectedDate = day.date
                            } label: {
                                VStack(spacing: 1) {
                                    Text("\(day.day)")
                                        .font(.system(size: 13, weight: day.isToday ? .bold : .medium))
                                        .foregroundStyle(
                                            day.isCurrentMonth
                                                ? MoWalkTheme.textPrimary
                                                : MoWalkTheme.textDim.opacity(0.3)
                                        )
                                    if day.steps > 0 && day.isCurrentMonth {
                                        Text("\(day.steps)")
                                            .font(.system(size: 7))
                                            .foregroundStyle(MoWalkTheme.textDim)
                                            .lineLimit(1)
                                    }
                                }
                                .frame(maxWidth: .infinity)
                                .aspectRatio(1, contentMode: .fill)
                                .background(
                                    day.isCurrentMonth
                                        ? stepHeatColor(day.steps, maxSteps: maxSteps)
                                        : Color.clear
                                )
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                                .overlay(
                                    day.date == viewModel.selectedDate
                                        ? RoundedRectangle(cornerRadius: 8)
                                            .stroke(MoWalkTheme.accent, lineWidth: 2)
                                        : nil
                                )
                                .overlay(
                                    day.isToday
                                        ? RoundedRectangle(cornerRadius: 8)
                                            .stroke(MoWalkTheme.accentLight, lineWidth: 1)
                                        : nil
                                )
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 16)

                    if let selected = viewModel.selectedDate,
                       let day = viewModel.days.first(where: { $0.date == selected }),
                       day.steps > 0 {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(formatDisplayDate(selected))
                                .font(.system(size: 12, weight: .medium))
                                .foregroundStyle(MoWalkTheme.textMuted)
                            Text("\(day.steps) steps")
                                .font(.system(size: 22, weight: .bold).monospacedDigit())
                                .foregroundStyle(MoWalkTheme.accentLight)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(16)
                        .background(MoWalkTheme.surface2)
                        .clipShape(RoundedRectangle(cornerRadius: MoWalkTheme.radiusSM))
                        .padding(.horizontal, 20)
                        .transition(.opacity)
                    }
                }
                .padding(.bottom, 32)
            }
        }
        .background(MoWalkTheme.bg)
    }

    private var maxSteps: Int {
        viewModel.days.map({ $0.steps }).max() ?? 1
    }

    private func stepHeatColor(_ steps: Int, maxSteps: Int) -> Color {
        guard maxSteps > 0, steps > 0 else { return .clear }
        let intensity = Double(steps) / Double(maxSteps)
        return MoWalkTheme.stepGreen.opacity(0.05 + intensity * 0.5)
    }

    private func formatDisplayDate(_ iso: String) -> String {
        guard let date = Date.dateFromISO(iso) else { return iso }
        let formatter = DateFormatter()
        formatter.dateFormat = "EEEE, MMM d, yyyy"
        return formatter.string(from: date)
    }
}
