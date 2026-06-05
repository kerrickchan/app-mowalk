import Foundation

final class StepDeltaCalculator {
    private var bootOffset: Int?
    private var previousDayTotal: Int = 0
    private var lastKnownToday: Int = 0

    func setYesterdayTotal(_ total: Int) {
        previousDayTotal = total
    }

    func update(cumulativeSteps: Int, yesterdayTotal: Int) -> Int {
        previousDayTotal = yesterdayTotal

        guard let offset = bootOffset else {
            bootOffset = cumulativeSteps
            return 0
        }

        let rawToday = cumulativeSteps - offset - yesterdayTotal
        let todaySteps = max(rawToday, lastKnownToday)
        lastKnownToday = todaySteps
        return todaySteps
    }

    func onDeviceReboot(cumulativeSteps: Int, yesterdayTotal: Int) -> Int {
        bootOffset = cumulativeSteps
        previousDayTotal = yesterdayTotal
        lastKnownToday = 0
        return 0
    }

    func resetForNewDay(yesterdayTotal: Int) {
        bootOffset = nil
        previousDayTotal = yesterdayTotal
        lastKnownToday = 0
    }

    func getLastKnownToday() -> Int { lastKnownToday }
    func getYesterdayTotal() -> Int { previousDayTotal }
}
