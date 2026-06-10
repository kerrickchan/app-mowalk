import XCTest
import SwiftData
@testable import MoWalk

final class PersistenceServiceTests: XCTestCase {

    @MainActor
    func test_getTodayEntryReturnsNilForEmptyDatabase() throws {
        let service = PersistenceService(useInMemoryStore: true)
        let entry = try service.getTodayEntry(date: "2024-06-01")
        XCTAssertNil(entry)
    }

    @MainActor
    func test_upsertAndRetrieveEntry() throws {
        let service = PersistenceService(useInMemoryStore: true)
        let entry = StepEntry(date: "2024-06-01", steps: 5000, distance: 2.5, calories: 250.0)
        try service.upsert(entry)

        let retrieved = try service.getTodayEntry(date: "2024-06-01")
        XCTAssertEqual(retrieved?.steps, 5000)
        XCTAssertEqual(retrieved?.distance, 2.5)
        XCTAssertEqual(retrieved?.calories, 250.0)
    }

    @MainActor
    func test_upsertUpdatesExistingEntry() throws {
        let service = PersistenceService(useInMemoryStore: true)
        let first = StepEntry(date: "2024-06-01", steps: 1000, distance: 1.0, calories: 100.0)
        try service.upsert(first)

        let second = StepEntry(date: "2024-06-01", steps: 8000, distance: 4.0, calories: 400.0)
        try service.upsert(second)

        let retrieved = try service.getTodayEntry(date: "2024-06-01")
        XCTAssertEqual(retrieved?.steps, 8000)
        XCTAssertEqual(retrieved?.distance, 4.0)
        XCTAssertEqual(retrieved?.calories, 400.0)
    }

    @MainActor
    func test_getEntriesFiltersByDateRange() throws {
        let service = PersistenceService(useInMemoryStore: true)
        try service.upsert(StepEntry(date: "2024-06-01", steps: 1000))
        try service.upsert(StepEntry(date: "2024-06-02", steps: 2000))
        try service.upsert(StepEntry(date: "2024-06-03", steps: 3000))
        try service.upsert(StepEntry(date: "2024-06-04", steps: 4000))
        try service.upsert(StepEntry(date: "2024-06-05", steps: 5000))

        let range = try service.getEntries(from: "2024-06-02", to: "2024-06-04")
        XCTAssertEqual(range.count, 3)
        XCTAssertEqual(range[0].date, "2024-06-02")
        XCTAssertEqual(range[2].date, "2024-06-04")
    }

    @MainActor
    func test_getAllEntriesReturnsSortedByDate() throws {
        let service = PersistenceService(useInMemoryStore: true)
        try service.upsert(StepEntry(date: "2024-06-03", steps: 3000))
        try service.upsert(StepEntry(date: "2024-06-01", steps: 1000))
        try service.upsert(StepEntry(date: "2024-06-02", steps: 2000))

        let all = try service.getAllEntries()
        XCTAssertEqual(all.count, 3)
        XCTAssertEqual(all[0].date, "2024-06-01")
        XCTAssertEqual(all[1].date, "2024-06-02")
        XCTAssertEqual(all[2].date, "2024-06-03")
    }

    @MainActor
    func test_deleteAllRemovesEverything() throws {
        let service = PersistenceService(useInMemoryStore: true)
        try service.upsert(StepEntry(date: "2024-06-01", steps: 1000))
        try service.upsert(StepEntry(date: "2024-06-02", steps: 2000))

        try service.deleteAll()

        let all = try service.getAllEntries()
        XCTAssertTrue(all.isEmpty)
    }

    @MainActor
    func test_getProfileCreatesDefaultWhenEmpty() throws {
        let service = PersistenceService(useInMemoryStore: true)
        let profile = try service.getProfile()
        XCTAssertNotNil(profile)
        XCTAssertEqual(profile?.dailyStepGoal, 8000)
        XCTAssertTrue(profile?.hkSyncEnabled == true)
    }

    @MainActor
    func test_updateProfileChangesValues() throws {
        let service = PersistenceService(useInMemoryStore: true)
        try service.updateProfile(height: 175, weight: 70, dailyStepGoal: 10000, hkSyncEnabled: false)

        let profile = try service.getProfile()
        XCTAssertEqual(profile?.height, 175)
        XCTAssertEqual(profile?.weight, 70)
        XCTAssertEqual(profile?.dailyStepGoal, 10000)
        XCTAssertEqual(profile?.hkSyncEnabled, false)
    }

    @MainActor
    func test_isManuallyEditedPersisted() throws {
        let service = PersistenceService(useInMemoryStore: true)
        let entry = StepEntry(date: "2024-06-01", steps: 5000, isManuallyEdited: true)
        try service.upsert(entry)

        let retrieved = try service.getTodayEntry(date: "2024-06-01")
        XCTAssertEqual(retrieved?.isManuallyEdited, true)
    }
}
