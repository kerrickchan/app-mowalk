import SwiftUI

struct StepRingView: View {
    let currentSteps: Int
    let goal: Int
    let size: CGFloat

    init(currentSteps: Int, goal: Int, size: CGFloat = 200) {
        self.currentSteps = currentSteps
        self.goal = goal
        self.size = size
    }

    private var progress: Double {
        goal > 0 ? min(Double(currentSteps) / Double(goal), 1.0) : 0
    }

    private var ringRadius: CGFloat { size / 2 - 16 }

    var body: some View {
        ZStack {
            Circle()
                .stroke(MoWalkTheme.surface2, lineWidth: 4)
                .frame(width: size - 8, height: size - 8)

            Circle()
                .trim(from: 0, to: progress)
                .stroke(
                    AngularGradient(
                        gradient: Gradient(colors: [MoWalkTheme.accent, MoWalkTheme.accentLight]),
                        center: .center,
                        startAngle: .degrees(-90),
                        endAngle: .degrees(270)
                    ),
                    style: StrokeStyle(lineWidth: 4, lineCap: .round)
                )
                .rotationEffect(.degrees(-90))
                .frame(width: size - 8, height: size - 8)
                .shadow(color: MoWalkTheme.accentGlow, radius: 8)

            VStack(spacing: 6) {
                Text("\(currentSteps)")
                    .font(.custom("Playfair Display", size: 48).weight(.bold))
                    .foregroundStyle(
                        LinearGradient(
                            colors: [.white, MoWalkTheme.accentLight],
                            startPoint: .top,
                            endPoint: .bottom
                        )
                    )
                    .lineLimit(1)
                    .minimumScaleFactor(0.5)

                Text("Steps Today")
                    .font(.system(size: 12, weight: .medium, design: .default))
                    .foregroundStyle(MoWalkTheme.textMuted)
                    .textCase(.uppercase)
            }
        }
        .frame(width: size, height: size)
    }
}
