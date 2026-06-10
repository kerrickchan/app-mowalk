import Observation
import SwiftUI

@MainActor
@Observable
final class SettingsViewModel {
    var height: Double?
    var weight: Double?
    var dailyStepGoal: Int = 8000
    var hkSyncEnabled: Bool = true
    var hkAvailable: Bool = false
    var stepCountingAvailable: Bool = false
    var showClearConfirmation: Bool = false

    private let persistence: PersistenceService
    private let stepService: StepCounterService
    private let healthKit: HealthKitService

    init(persistence: PersistenceService, stepService: StepCounterService, healthKit: HealthKitService) {
        self.persistence = persistence
        self.stepService = stepService
        self.healthKit = healthKit
        loadProfile()
    }

    func loadProfile() {
        hkAvailable = healthKit.isAvailable
        stepCountingAvailable = stepService.isStepCountingAvailable

        do {
            if let profile = try persistence.getProfile() {
                height = profile.height
                weight = profile.weight
                dailyStepGoal = profile.dailyStepGoal
                hkSyncEnabled = profile.hkSyncEnabled
            }
        } catch {}
    }

    func saveProfile() {
        do {
            try persistence.updateProfile(
                height: height,
                weight: weight,
                dailyStepGoal: dailyStepGoal,
                hkSyncEnabled: hkSyncEnabled
            )
        } catch {}
    }

    func toggleHKSync(enabled: Bool) {
        hkSyncEnabled = enabled
        saveProfile()
        if enabled {
            Task {
                try? await healthKit.requestAuthorization()
            }
        }
    }

    func clearAllData() {
        do {
            try persistence.deleteAll()
            showClearConfirmation = false
        } catch {}
    }
}
