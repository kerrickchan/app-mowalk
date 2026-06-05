import Observation
import SwiftUI

@MainActor
@Observable
final class DashboardViewModel {
    var todaySteps: Int = 0
    var distance: Double = 0
    var calories: Double = 0
    var dailyGoal: Int = 8000
    var progressPercent: Double = 0
    var isStepCountingAvailable: Bool = false
    var isRefreshing: Bool = false
    var error: String?

    private let persistence: StepPersistence
    private let stepService: StepCounting
    private let healthKit: HealthDataAccess

    init(persistence: StepPersistence, stepService: StepCounting, healthKit: HealthDataAccess) {
        self.persistence = persistence
        self.stepService = stepService
        self.healthKit = healthKit
        Task { await loadInitialData() }
    }

    func loadInitialData() async {
        isStepCountingAvailable = stepService.isStepCountingAvailable
        let today = Date.todayISO

        do {
            if let profile = try persistence.getProfile() {
                dailyGoal = profile.dailyStepGoal
            }
            if let entry = try persistence.getTodayEntry(date: today) {
                todaySteps = entry.steps
                distance = entry.distance
                calories = entry.calories
            }
            recomputeProgress()
        } catch {
            self.error = error.localizedDescription
        }
    }

    func refresh() async {
        isRefreshing = true
        defer { isRefreshing = false }

        let today = Date.todayISO
        do {
            let cmSteps = try await stepService.queryTodaySteps()
            let cmDistance = try await stepService.queryTodayDistance()

            if let profile = try persistence.getProfile() {
                let cal = estimateCalories(steps: cmSteps, distance: cmDistance, profile: profile)
                let entry = StepEntry(date: today, steps: cmSteps, distance: cmDistance, calories: cal)
                try persistence.upsert(entry)

                todaySteps = cmSteps
                distance = cmDistance
                calories = cal
                dailyGoal = profile.dailyStepGoal
            }
            recomputeProgress()

            if let profile = try persistence.getProfile(), profile.hkSyncEnabled {
                try await healthKit.requestAuthorization()
            }
        } catch {
            self.error = error.localizedDescription
        }
    }

    func clearError() {
        error = nil
    }

    private func recomputeProgress() {
        progressPercent = dailyGoal > 0 ? min(Double(todaySteps) / Double(dailyGoal), 1.0) : 0
    }

    private func estimateCalories(steps: Int, distance: Double, profile: UserProfile) -> Double {
        guard let weight = profile.weight else { return 0 }
        let km = distance / 1000.0
        return km * weight * 0.75
    }
}
