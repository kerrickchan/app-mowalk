import SwiftUI

struct ProgressCardView: View {
    let currentSteps: Int
    let goal: Int

    private var progress: Double {
        goal > 0 ? min(Double(currentSteps) / Double(goal), 1.0) : 0
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            GeometryReader { geometry in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(MoWalkTheme.surface2)
                        .frame(height: 8)

                    RoundedRectangle(cornerRadius: 4)
                        .fill(
                            LinearGradient(
                                colors: [MoWalkTheme.accent, MoWalkTheme.accentLight],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .frame(width: geometry.size.width * progress, height: 8)
                }
            }
            .frame(height: 8)

            HStack {
                Text("\(Int(progress * 100))% of \(goal) goal")
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(MoWalkTheme.textMuted)
                Spacer()
                if progress >= 1.0 {
                    Text("Goal reached!")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundStyle(MoWalkTheme.stepGreen)
                }
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
