import Observation
import SwiftUI

struct CalendarDay: Identifiable, Hashable {
    let id: String
    let day: Int
    let date: String
    let steps: Int
    let isToday: Bool
    let isCurrentMonth: Bool
}

@MainActor
@Observable
final class CalendarViewModel {
    var days: [CalendarDay] = []
    var selectedDate: String?
    var currentYear: Int
    var currentMonth: Int
    var monthName: String = ""
    var previousMonthName: String = ""
    var nextMonthName: String = ""

    private let persistence: PersistenceService
    private let calendar = Calendar.current

    init(persistence: PersistenceService) {
        self.persistence = persistence
        let now = Date()
        self.currentYear = calendar.component(.year, from: now)
        self.currentMonth = calendar.component(.month, from: now)
        loadMonth()
    }

    func previousMonth() {
        if currentMonth == 1 {
            currentMonth = 12
            currentYear -= 1
        } else {
            currentMonth -= 1
        }
        loadMonth()
    }

    func nextMonth() {
        if currentMonth == 12 {
            currentMonth = 1
            currentYear += 1
        } else {
            currentMonth += 1
        }
        loadMonth()
    }

    func goToToday() {
        let now = Date()
        currentYear = calendar.component(.year, from: now)
        currentMonth = calendar.component(.month, from: now)
        loadMonth()
    }

    func loadMonth() {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM yyyy"
        var components = DateComponents()
        components.year = currentYear
        components.month = currentMonth
        components.day = 1
        if let date = calendar.date(from: components) {
            monthName = formatter.string(from: date)
        }

        if let prevDate = calendar.date(byAdding: .month, value: -1, to: calendar.date(from: components)!) {
            previousMonthName = formatter.string(from: prevDate)
        }

        if let nextDate = calendar.date(byAdding: .month, value: 1, to: calendar.date(from: components)!) {
            nextMonthName = formatter.string(from: nextDate)
        }

        do {
            let entries = try persistence.getMonthlyEntries(year: currentYear, month: currentMonth)
            days = generateDays(entries: entries)
        } catch {
            days = generateDays(entries: [])
        }
    }

    private func generateDays(entries: [StepEntry]) -> [CalendarDay] {
        var result: [CalendarDay] = []
        var components = DateComponents()
        components.year = currentYear
        components.month = currentMonth
        components.day = 1

        guard let firstOfMonth = calendar.date(from: components) else { return [] }

        let weekday = calendar.component(.weekday, from: firstOfMonth)
        let daysInMonth = calendar.range(of: .day, in: .month, for: firstOfMonth)?.count ?? 30

        let prevMonthDays = weekday - 1
        let isoFormatter = ISO8601DateFormatter()
        isoFormatter.formatOptions = [.withFullDate, .withDashSeparatorInDate]
        let todayISO = Date.todayISO
        let stepMap = Dictionary(uniqueKeysWithValues: entries.map { ($0.date, $0.steps) })

        for i in 0..<prevMonthDays {
            let day = -(prevMonthDays - 1) + i
            let date = calendar.date(byAdding: .day, value: day, to: firstOfMonth)!
            let iso = isoFormatter.string(from: date)
            result.append(CalendarDay(
                id: iso,
                day: calendar.component(.day, from: date),
                date: iso,
                steps: 0,
                isToday: false,
                isCurrentMonth: false
            ))
        }

        for day in 1...daysInMonth {
            let date = calendar.date(byAdding: .day, value: day - 1, to: firstOfMonth)!
            let iso = isoFormatter.string(from: date)
            result.append(CalendarDay(
                id: iso,
                day: day,
                date: iso,
                steps: stepMap[iso] ?? 0,
                isToday: iso == todayISO,
                isCurrentMonth: true
            ))
        }

        let remainingDays = 42 - result.count
        if remainingDays > 0 {
            for day in 1...remainingDays {
                let date = calendar.date(byAdding: .day, value: daysInMonth - 1 + day, to: firstOfMonth)!
                let iso = isoFormatter.string(from: date)
                result.append(CalendarDay(
                    id: iso,
                    day: calendar.component(.day, from: date),
                    date: iso,
                    steps: 0,
                    isToday: false,
                    isCurrentMonth: false
                ))
            }
        }

        return result
    }
}
