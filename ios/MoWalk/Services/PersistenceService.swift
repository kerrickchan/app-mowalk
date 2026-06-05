import Foundation
import SwiftData

final class PersistenceService {
    private let modelContainer: ModelContainer
    private let modelContext: ModelContext

    init() {
        let schema = Schema([StepEntry.self, UserProfile.self])
        let config = ModelConfiguration(schema: schema, isStoredInMemoryOnly: false)
        do {
            modelContainer = try ModelContainer(for: schema, configurations: [config])
            modelContext = ModelContext(modelContainer)
        } catch {
            fatalError("Failed to create ModelContainer: \(error)")
        }
    }

    var container: ModelContainer { modelContainer }
    var context: ModelContext { modelContext }

    func getTodayEntry(date: String = Date.todayISO) throws -> StepEntry? {
        let descriptor = FetchDescriptor<StepEntry>()
        let all = try modelContext.fetch(descriptor)
        return all.first { $0.date == date }
    }

    func getEntries(from start: String, to end: String) throws -> [StepEntry] {
        let descriptor = FetchDescriptor<StepEntry>(sortBy: [SortDescriptor(\.date)])
        let all = try modelContext.fetch(descriptor)
        return all.filter { $0.date >= start && $0.date <= end }
    }

    func getWeeklyEntries() throws -> [StepEntry] {
        let range = Date.weekRange()
        return try getEntries(from: range.start, to: range.end)
    }

    func getMonthlyEntries(year: Int, month: Int) throws -> [StepEntry] {
        let range = Date.monthRange(year: year, month: month)
        return try getEntries(from: range.start, to: range.end)
    }

    func getAllEntries() throws -> [StepEntry] {
        let descriptor = FetchDescriptor<StepEntry>(sortBy: [SortDescriptor(\.date)])
        return try modelContext.fetch(descriptor)
    }

    func upsert(_ entry: StepEntry) throws {
        let descriptor = FetchDescriptor<StepEntry>()
        let all = try modelContext.fetch(descriptor)
        if let existing = all.first(where: { $0.date == entry.date }) {
            existing.steps = entry.steps
            existing.distance = entry.distance
            existing.calories = entry.calories
            existing.isManuallyEdited = entry.isManuallyEdited
        } else {
            modelContext.insert(entry)
        }
        try modelContext.save()
    }

    func deleteAll() throws {
        try modelContext.delete(model: StepEntry.self)
        try modelContext.save()
    }

    func getProfile() throws -> UserProfile? {
        let descriptor = FetchDescriptor<UserProfile>()
        let profiles = try modelContext.fetch(descriptor)
        if let existing = profiles.first {
            return existing
        }
        let profile = UserProfile()
        modelContext.insert(profile)
        try modelContext.save()
        return profile
    }

    func updateProfile(height: Double?, weight: Double?, dailyStepGoal: Int, hkSyncEnabled: Bool) throws {
        if let profile = try getProfile() {
            profile.height = height
            profile.weight = weight
            profile.dailyStepGoal = dailyStepGoal
            profile.hkSyncEnabled = hkSyncEnabled
            try modelContext.save()
        }
    }
}
