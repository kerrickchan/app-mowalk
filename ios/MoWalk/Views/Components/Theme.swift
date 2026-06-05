import SwiftUI

enum MoWalkTheme {
    static let bg = Color(hex: "#0c0a09")
    static let surface = Color(hex: "#161210")
    static let surface2 = Color(hex: "#1e1916")
    static let surface3 = Color(hex: "#28221e")

    static let accent = Color(hex: "#c87533")
    static let accentLight = Color(hex: "#e89556")
    static let accentGlow = Color(hex: "#c87533").opacity(0.3)

    static let textPrimary = Color(hex: "#ece3d9")
    static let textMuted = Color(hex: "#8a7a6a")
    static let textDim = Color(hex: "#5a4e42")

    static let stepGreen = Color(hex: "#4CAF50")
    static let errorRed = Color(hex: "#e05050")

    static let radius: CGFloat = 16
    static let radiusSM: CGFloat = 10
}

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r, g, b: Double
        switch hex.count {
        case 6:
            r = Double((int >> 16) & 0xFF) / 255
            g = Double((int >> 8) & 0xFF) / 255
            b = Double(int & 0xFF) / 255
        default:
            r = 1; g = 1; b = 1
        }
        self.init(red: r, green: g, blue: b)
    }
}
