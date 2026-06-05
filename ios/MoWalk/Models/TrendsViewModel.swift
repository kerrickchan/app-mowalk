import Observation
import SwiftUI

enum TrendsPeriod: String, CaseIterable {
    case week = "This Week"
    case month = "This Month"
}

@MainActor
@Observable
final class TrendsViewModel {
    var dataPoints: [StepEntry] = []
    var period: TrendsPeriod = .week
    var selectedDay: StepEntry?
    var isLoading: Bool = false

    private let persistence: PersistenceService

    init(persistence: PersistenceService) {
        self.persistence = persistence
        loadData()
    }

    func setPeriod(_ newPeriod: TrendsPeriod) {
        period = newPeriod
        loadData()
    }

    func loadData() {
        isLoading = true
        defer { isLoading = false }

        do {
            switch period {
            case .week:
                dataPoints = try persistence.getWeeklyEntries()
            case .month:
                let calendar = Calendar.current
                let now = Date()
                let year = calendar.component(.year, from: now)
                let month = calendar.component(.month, from: now)
                dataPoints = try persistence.getMonthlyEntries(year: year, month: month)
            }
        } catch {
            dataPoints = []
        }
    }

    func selectDay(_ day: StepEntry?) {
        selectedDay = day
    }
}
