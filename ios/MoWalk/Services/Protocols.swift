import Foundation

protocol StepPersistence: AnyObject, Sendable {
    func getTodayEntry(date: String) throws -> StepEntry?
    func getEntries(from start: String, to end: String) throws -> [StepEntry]
    func getWeeklyEntries() throws -> [StepEntry]
    func getMonthlyEntries(year: Int, month: Int) throws -> [StepEntry]
    func getAllEntries() throws -> [StepEntry]
    func upsert(_ entry: StepEntry) throws
    func deleteAll() throws
    func getProfile() throws -> UserProfile?
    func updateProfile(height: Double?, weight: Double?, dailyStepGoal: Int, hkSyncEnabled: Bool) throws
}

protocol StepCounting: AnyObject, Sendable {
    var isStepCountingAvailable: Bool { get }
    func queryTodaySteps() async throws -> Int
    func queryTodayDistance() async throws -> Double
    func requestAuthorization() async -> Bool
}

protocol HealthDataAccess: AnyObject, Sendable {
    var isAvailable: Bool { get }
    func requestAuthorization() async throws
}

extension PersistenceService: @unchecked Sendable {}
extension PersistenceService: StepPersistence {}
extension StepCounterService: StepCounting {}
extension HealthKitService: HealthDataAccess {}
