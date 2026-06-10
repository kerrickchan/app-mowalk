import Foundation
@testable import MoWalk

final class MockPersistence: StepPersistence, @unchecked Sendable {
    var todayEntry: StepEntry?
    var profile: UserProfile?
    var entries: [StepEntry] = []
    var upsertCallCount = 0
    var lastUpsertedEntry: StepEntry?
    var deleteAllCalled = false

    func getTodayEntry(date: String) throws -> StepEntry? { todayEntry }
    func getEntries(from start: String, to end: String) throws -> [StepEntry] { entries }
    func getWeeklyEntries() throws -> [StepEntry] { entries }
    func getMonthlyEntries(year: Int, month: Int) throws -> [StepEntry] { entries }
    func getAllEntries() throws -> [StepEntry] { entries }
    func upsert(_ entry: StepEntry) throws { upsertCallCount += 1; lastUpsertedEntry = entry }
    func deleteAll() throws { deleteAllCalled = true }

    func getProfile() throws -> UserProfile? {
        if let profile { return profile }
        let p = UserProfile()
        self.profile = p
        return p
    }

    func updateProfile(height: Double?, weight: Double?, dailyStepGoal: Int, hkSyncEnabled: Bool) throws {
        profile?.height = height
        profile?.weight = weight
        profile?.dailyStepGoal = dailyStepGoal
        profile?.hkSyncEnabled = hkSyncEnabled
    }
}

final class MockStepCounting: StepCounting, @unchecked Sendable {
    var isStepCountingAvailable: Bool = true
    var queryStepsResult: Result<Int, Error> = .success(0)
    var queryDistanceResult: Result<Double, Error> = .success(0)
    var authResult: Bool = true

    func queryTodaySteps() async throws -> Int { try queryStepsResult.get() }
    func queryTodayDistance() async throws -> Double { try queryDistanceResult.get() }
    func requestAuthorization() async -> Bool { authResult }
}

final class MockHealthData: HealthDataAccess, @unchecked Sendable {
    var isAvailable: Bool = true
    var authError: Error?

    func requestAuthorization() async throws {
        if let error = authError { throw error }
    }
}
