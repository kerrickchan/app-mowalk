import Foundation
import SwiftData

@Model
final class UserProfile {
    @Attribute(.unique) var id: Int
    var height: Double?
    var weight: Double?
    var dailyStepGoal: Int
    var hkSyncEnabled: Bool

    init(id: Int = 1, height: Double? = nil, weight: Double? = nil, dailyStepGoal: Int = 8000, hkSyncEnabled: Bool = true) {
        self.id = id
        self.height = height
        self.weight = weight
        self.dailyStepGoal = dailyStepGoal
        self.hkSyncEnabled = hkSyncEnabled
    }
}
