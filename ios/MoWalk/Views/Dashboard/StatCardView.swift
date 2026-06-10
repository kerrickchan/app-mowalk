import SwiftUI

struct StatCardView: View {
    let value: String
    let unit: String
    let label: String
    let icon: String

    var body: some View {
        VStack(spacing: 4) {
            HStack(alignment: .lastTextBaseline, spacing: 2) {
                Text(value)
                    .font(.system(size: 18, weight: .bold, design: .rounded))
                    .foregroundStyle(MoWalkTheme.textPrimary)
                Text(unit)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(MoWalkTheme.textMuted)
            }
            Text(label)
                .font(.system(size: 11, weight: .medium))
                .foregroundStyle(MoWalkTheme.textDim)
                .textCase(.uppercase)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .padding(.horizontal, 12)
        .background(MoWalkTheme.surface)
        .clipShape(RoundedRectangle(cornerRadius: MoWalkTheme.radiusSM))
        .overlay(
            RoundedRectangle(cornerRadius: MoWalkTheme.radiusSM)
                .stroke(MoWalkTheme.surface2, lineWidth: 1)
        )
    }
}
