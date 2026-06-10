import SwiftUI
import SwiftData

@main
struct MoWalkApp: App {
    @State private var persistenceService = PersistenceService()

    var body: some Scene {
        WindowGroup {
            AppTabView(persistence: persistenceService)
                .preferredColorScheme(.dark)
                .background(MoWalkTheme.bg)
        }
    }
}
