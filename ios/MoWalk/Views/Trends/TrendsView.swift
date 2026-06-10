import SwiftUI
import Charts

struct TrendsView: View {
    @State private var viewModel: TrendsViewModel

    init(viewModel: TrendsViewModel) {
        self.viewModel = viewModel
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                BrandHeader(date: Date.todayISO)

                VStack(spacing: 16) {
                    Picker("Period", selection: Binding(
                        get: { viewModel.period },
                        set: { viewModel.setPeriod($0) }
                    )) {
                        ForEach(TrendsPeriod.allCases, id: \.self) { period in
                            Text(period.rawValue).tag(period)
                        }
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal, 20)

                    if viewModel.dataPoints.isEmpty {
                        VStack(spacing: 12) {
                            Image(systemName: "chart.line.uptrend.xyaxis")
                                .font(.system(size: 40))
                                .foregroundStyle(MoWalkTheme.textDim)
                            Text("No data yet")
                                .font(.system(size: 15, weight: .medium))
                                .foregroundStyle(MoWalkTheme.textMuted)
                            Text("Start walking to see your trends")
                                .font(.system(size: 13))
                                .foregroundStyle(MoWalkTheme.textDim)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 60)
                    } else {
                        Chart(viewModel.dataPoints, id: \.date) { entry in
                            LineMark(
                                x: .value("Date", chartDate(entry.date)),
                                y: .value("Steps", entry.steps)
                            )
                            .foregroundStyle(
                                LinearGradient(
                                    colors: [MoWalkTheme.accent, MoWalkTheme.accentLight],
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .lineStyle(StrokeStyle(lineWidth: 2.5, lineCap: .round))

                            AreaMark(
                                x: .value("Date", chartDate(entry.date)),
                                y: .value("Steps", entry.steps)
                            )
                            .foregroundStyle(
                                LinearGradient(
                                    colors: [MoWalkTheme.accent.opacity(0.3), MoWalkTheme.accent.opacity(0.02)],
                                    startPoint: .top,
                                    endPoint: .bottom
                                )
                            )

                            PointMark(
                                x: .value("Date", chartDate(entry.date)),
                                y: .value("Steps", entry.steps)
                            )
                            .foregroundStyle(MoWalkTheme.accentLight)
                            .symbolSize(16)
                        }
                        .chartXAxis {
                            AxisMarks { value in
                                AxisValueLabel()
                                    .foregroundStyle(MoWalkTheme.textDim)
                            }
                        }
                        .chartYAxis {
                            AxisMarks { value in
                                AxisValueLabel()
                                    .foregroundStyle(MoWalkTheme.textDim)
                                AxisGridLine()
                                    .foregroundStyle(MoWalkTheme.surface3)
                            }
                        }
                        .frame(height: 240)
                        .padding(.horizontal, 20)
                        .chartOverlay { proxy in
                            GeometryReader { geometry in
                                Rectangle()
                                    .fill(.clear)
                                    .contentShape(Rectangle())
                                    .gesture(
                                        DragGesture(minimumDistance: 0)
                                            .onChanged { value in
                                                let location = value.location
                                                guard let date: String = proxy.value(atX: location.x) else { return }
                                                if let entry = viewModel.dataPoints.first(where: { $0.date == date }) {
                                                    viewModel.selectDay(entry)
                                                }
                                            }
                                            .onEnded { _ in
                                                viewModel.selectDay(nil)
                                            }
                                    )
                            }
                        }
                    }

                    if let selected = viewModel.selectedDay {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(formatDisplayDate(selected.date))
                                .font(.system(size: 12, weight: .medium))
                                .foregroundStyle(MoWalkTheme.textMuted)
                            Text("\(selected.steps) steps")
                                .font(.system(size: 28, weight: .bold).monospacedDigit())
                                .foregroundStyle(MoWalkTheme.accentLight)
                            HStack(spacing: 16) {
                                Text(String(format: "%.1f km", selected.distance / 1000))
                                    .font(.system(size: 13))
                                    .foregroundStyle(MoWalkTheme.textDim)
                                Text("\(Int(selected.calories)) kcal")
                                    .font(.system(size: 13))
                                    .foregroundStyle(MoWalkTheme.textDim)
                            }
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(16)
                        .background(MoWalkTheme.surface2)
                        .clipShape(RoundedRectangle(cornerRadius: MoWalkTheme.radiusSM))
                        .padding(.horizontal, 20)
                        .transition(.opacity.combined(with: .move(edge: .bottom)))
                    }
                }
                .padding(.bottom, 32)
            }
        }
        .background(MoWalkTheme.bg)
    }

    private func chartDate(_ iso: String) -> Date {
        Date.dateFromISO(iso) ?? Date()
    }

    private func formatDisplayDate(_ iso: String) -> String {
        guard let date = Date.dateFromISO(iso) else { return iso }
        let formatter = DateFormatter()
        formatter.dateFormat = "EEEE, MMM d, yyyy"
        return formatter.string(from: date)
    }
}
