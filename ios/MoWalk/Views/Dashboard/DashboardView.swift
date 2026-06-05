import SwiftUI

struct DashboardView: View {
    @State private var viewModel: DashboardViewModel

    init(viewModel: DashboardViewModel) {
        self.viewModel = viewModel
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                BrandHeader(date: Date.todayISO)

                VStack(spacing: 20) {
                    StepRingView(currentSteps: viewModel.todaySteps, goal: viewModel.dailyGoal)
                        .padding(.top, 20)

                    HStack(spacing: 12) {
                        StatCardView(
                            value: String(format: "%.1f", viewModel.distance / 1000),
                            unit: "km",
                            label: "Distance",
                            icon: "figure.walk"
                        )
                        StatCardView(
                            value: "\(Int(viewModel.calories))",
                            unit: "kcal",
                            label: "Calories",
                            icon: "flame"
                        )
                        StatCardView(
                            value: "\(Int(viewModel.progressPercent * 100))",
                            unit: "%",
                            label: "Goal",
                            icon: "target"
                        )
                    }
                    .padding(.horizontal, 20)

                    ProgressCardView(currentSteps: viewModel.todaySteps, goal: viewModel.dailyGoal)
                        .padding(.horizontal, 20)

                    if let error = viewModel.error {
                        HStack {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundStyle(MoWalkTheme.errorRed)
                            Text(error)
                                .font(.system(size: 13))
                                .foregroundStyle(MoWalkTheme.textPrimary)
                            Spacer()
                            Button(action: { viewModel.clearError() }) {
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundStyle(MoWalkTheme.textMuted)
                            }
                        }
                        .padding(12)
                        .background(MoWalkTheme.surface)
                        .clipShape(RoundedRectangle(cornerRadius: MoWalkTheme.radiusSM))
                        .overlay(
                            RoundedRectangle(cornerRadius: MoWalkTheme.radiusSM)
                                .stroke(MoWalkTheme.errorRed.opacity(0.3), lineWidth: 1)
                        )
                        .padding(.horizontal, 20)
                    }

                    if !viewModel.isStepCountingAvailable {
                        HStack {
                            Image(systemName: "exclamationmark.triangle")
                                .foregroundStyle(.orange)
                            Text("Step counting not available on this device")
                                .font(.system(size: 13))
                                .foregroundStyle(MoWalkTheme.textMuted)
                        }
                        .padding(.horizontal, 20)
                    }
                }
                .padding(.bottom, 32)
            }
        }
        .refreshable {
            await viewModel.refresh()
        }
        .background(MoWalkTheme.bg)
    }
}
