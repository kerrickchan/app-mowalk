import SwiftUI

struct SettingsView: View {
    @State private var viewModel: SettingsViewModel

    init(viewModel: SettingsViewModel) {
        self.viewModel = viewModel
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                BrandHeader(date: Date.todayISO)

                VStack(alignment: .leading, spacing: 24) {
                    profileSection
                    healthKitSection
                    dangerSection
                    aboutSection
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 32)
            }
        }
        .background(MoWalkTheme.bg)
        .alert("Clear All Data?", isPresented: $viewModel.showClearConfirmation) {
            Button("Cancel", role: .cancel) {}
            Button("Delete All", role: .destructive) {
                viewModel.clearAllData()
            }
        } message: {
            Text("This will permanently delete all step history and profile settings. This action cannot be undone.")
        }
        .onChange(of: viewModel.dailyStepGoal) { _, _ in viewModel.saveProfile() }
        .onChange(of: viewModel.height) { _, _ in viewModel.saveProfile() }
        .onChange(of: viewModel.weight) { _, _ in viewModel.saveProfile() }
    }

    private var profileSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("Profile")

            VStack(spacing: 12) {
                labeledInput("Height (cm)", value: $viewModel.height)
                labeledInput("Weight (kg)", value: $viewModel.weight)

                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Daily Step Goal")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundStyle(MoWalkTheme.textMuted)
                            .textCase(.uppercase)
                    }
                    Spacer()
                    Stepper("\(viewModel.dailyStepGoal)", value: $viewModel.dailyStepGoal, in: 1000...50000, step: 1000)
                        .font(.system(size: 15))
                        .foregroundStyle(MoWalkTheme.textPrimary)
                }
            }
            .padding(16)
            .background(MoWalkTheme.surface)
            .clipShape(RoundedRectangle(cornerRadius: MoWalkTheme.radiusSM))
            .overlay(
                RoundedRectangle(cornerRadius: MoWalkTheme.radiusSM)
                    .stroke(MoWalkTheme.surface2, lineWidth: 1)
            )
        }
    }

    private var healthKitSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("Health Data")

            VStack(spacing: 0) {
                if viewModel.hkAvailable {
                    ToggleRow(
                        title: "Sync with Health",
                        subtitle: "Read and write step data to Apple Health",
                        isOn: Binding(
                            get: { viewModel.hkSyncEnabled },
                            set: { viewModel.toggleHKSync(enabled: $0) }
                        )
                    )
                } else {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Health Not Available")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundStyle(MoWalkTheme.textPrimary)
                            Text("Health app is not available on this device")
                                .font(.system(size: 12))
                                .foregroundStyle(MoWalkTheme.textDim)
                        }
                        Spacer()
                        Image(systemName: "heart.slash")
                            .foregroundStyle(MoWalkTheme.textDim)
                    }
                    .padding(.vertical, 14)
                }
            }
            .padding(16)
            .background(MoWalkTheme.surface)
            .clipShape(RoundedRectangle(cornerRadius: MoWalkTheme.radiusSM))
            .overlay(
                RoundedRectangle(cornerRadius: MoWalkTheme.radiusSM)
                    .stroke(MoWalkTheme.surface2, lineWidth: 1)
            )
        }
    }

    private var dangerSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("Data")

            Button(action: { viewModel.showClearConfirmation = true }) {
                HStack {
                    Image(systemName: "trash")
                    Text("Clear All Data")
                }
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(MoWalkTheme.errorRed)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 13)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(MoWalkTheme.errorRed.opacity(0.2), lineWidth: 1)
                )
            }
            .padding(16)
            .background(MoWalkTheme.surface)
            .clipShape(RoundedRectangle(cornerRadius: MoWalkTheme.radiusSM))
            .overlay(
                RoundedRectangle(cornerRadius: MoWalkTheme.radiusSM)
                    .stroke(MoWalkTheme.surface2, lineWidth: 1)
            )
        }
    }

    private var aboutSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            sectionHeader("About")
            Text("MoWalk 1.0.0")
                .font(.system(size: 13))
                .foregroundStyle(MoWalkTheme.textDim)
            Text("Phone-only pedometer. All data stays on your device.")
                .font(.system(size: 12))
                .foregroundStyle(MoWalkTheme.textDim)
        }
    }

    private func sectionHeader(_ title: String) -> some View {
        Text(title)
            .font(.system(size: 12, weight: .semibold))
            .foregroundStyle(MoWalkTheme.textMuted)
            .textCase(.uppercase)
    }

    private func labeledInput(_ label: String, value: Binding<Double?>) -> some View {
        HStack {
            Text(label)
                .font(.system(size: 14))
                .foregroundStyle(MoWalkTheme.textPrimary)
            Spacer()
            TextField("Not set", value: value, format: .number.grouping(.never))
                .keyboardType(.decimalPad)
                .multilineTextAlignment(.trailing)
                .font(.system(size: 15))
                .foregroundStyle(MoWalkTheme.textPrimary)
                .frame(width: 100)
        }
    }
}

struct ToggleRow: View {
    let title: String
    let subtitle: String
    @Binding var isOn: Bool

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundStyle(MoWalkTheme.textPrimary)
                Text(subtitle)
                    .font(.system(size: 12))
                    .foregroundStyle(MoWalkTheme.textDim)
            }
            Spacer()
            Toggle("", isOn: $isOn)
                .labelsHidden()
                .tint(MoWalkTheme.accent)
        }
        .padding(.vertical, 4)
    }
}
