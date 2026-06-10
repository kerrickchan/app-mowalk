import Foundation
import SwiftData

@Model
final class StepEntry {
    @Attribute(.unique) var date: String
    var steps: Int
    var distance: Double
    var calories: Double
    var isManuallyEdited: Bool

    init(date: String = Date.todayISO, steps: Int = 0, distance: Double = 0, calories: Double = 0, isManuallyEdited: Bool = false) {
        self.date = date
        self.steps = steps
        self.distance = distance
        self.calories = calories
        self.isManuallyEdited = isManuallyEdited
    }
}

extension Date {
    static var todayISO: String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withFullDate, .withDashSeparatorInDate]
        return formatter.string(from: Date())
    }

    static func dateFromISO(_ iso: String) -> Date? {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withFullDate, .withDashSeparatorInDate]
        return formatter.date(from: iso)
    }

    static func weekRange(from date: Date = Date()) -> (start: String, end: String) {
        let calendar = Calendar.current
        let weekday = calendar.component(.weekday, from: date)
        let daysToMonday = (weekday + 5) % 7
        let monday = calendar.startOfDay(for: calendar.date(byAdding: .day, value: -daysToMonday, to: date)!)
        let sunday = calendar.date(byAdding: .day, value: 6, to: monday)!
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withFullDate, .withDashSeparatorInDate]
        return (formatter.string(from: monday), formatter.string(from: sunday))
    }

    static func monthRange(year: Int, month: Int) -> (start: String, end: String) {
        let calendar = Calendar.current
        var components = DateComponents()
        components.year = year
        components.month = month
        components.day = 1
        let firstDay = calendar.date(from: components)!
        let lastDay = calendar.date(byAdding: DateComponents(month: 1, day: -1), to: firstDay)!
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withFullDate, .withDashSeparatorInDate]
        return (formatter.string(from: firstDay), formatter.string(from: lastDay))
    }
}
