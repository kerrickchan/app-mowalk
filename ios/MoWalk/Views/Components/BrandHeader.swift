import SwiftUI

struct BrandHeader: View {
    let date: String

    var body: some View {
        HStack {
            HStack(spacing: 0) {
                Text("Mo")
                    .font(.custom("Playfair Display", size: 22).italic())
                    .foregroundStyle(MoWalkTheme.accent)
                Text("Walk")
                    .font(.custom("Playfair Display", size: 22).weight(.semibold))
                    .foregroundStyle(MoWalkTheme.textPrimary)
            }
            Spacer()
            Text(formatDate(date))
                .font(.system(size: 12, weight: .medium, design: .default))
                .foregroundStyle(MoWalkTheme.textMuted)
                .textCase(.uppercase)
        }
        .padding(.horizontal, 20)
        .padding(.top, 8)
        .padding(.bottom, 4)
    }

    private func formatDate(_ iso: String) -> String {
        guard let date = Date.dateFromISO(iso) else { return iso }
        let formatter = DateFormatter()
        formatter.dateFormat = "EEEE, MMM d"
        return formatter.string(from: date)
    }
}
