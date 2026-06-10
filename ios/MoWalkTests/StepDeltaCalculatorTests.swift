import XCTest
@testable import MoWalk

final class StepDeltaCalculatorTests: XCTestCase {

    private var calculator: StepDeltaCalculator!

    override func setUp() {
        super.setUp()
        calculator = StepDeltaCalculator()
    }

    override func tearDown() {
        calculator = nil
        super.tearDown()
    }

    func test_firstUpdateCapturesBootOffsetAndReturnsZero() {
        let result = calculator.update(cumulativeSteps: 1000, yesterdayTotal: 500)
        XCTAssertEqual(result, 0)
        XCTAssertEqual(calculator.getLastKnownToday(), 0)
    }

    func test_secondUpdateComputesStepDeltaCorrectly() {
        _ = calculator.update(cumulativeSteps: 1000, yesterdayTotal: 0)
        let result = calculator.update(cumulativeSteps: 2800, yesterdayTotal: 300)
        XCTAssertEqual(result, 1500)
    }

    func test_stepsNeverDecreaseDueToMaxOfWithLastKnown() {
        _ = calculator.update(cumulativeSteps: 100, yesterdayTotal: 0)
        let step1 = calculator.update(cumulativeSteps: 1100, yesterdayTotal: 0)
        XCTAssertEqual(step1, 1000)

        let step2 = calculator.update(cumulativeSteps: 1100, yesterdayTotal: 500)
        XCTAssertEqual(step2, 1000)
    }

    func test_setYesterdayTotalUpdatesStoredValue() {
        calculator.setYesterdayTotal(4200)
        XCTAssertEqual(calculator.getYesterdayTotal(), 4200)
    }

    func test_resetForNewDayClearsBootOffsetAndLastKnown() {
        _ = calculator.update(cumulativeSteps: 1000, yesterdayTotal: 0)
        _ = calculator.update(cumulativeSteps: 3000, yesterdayTotal: 0)
        XCTAssertEqual(calculator.getLastKnownToday(), 2000)

        calculator.resetForNewDay(yesterdayTotal: 2000)

        XCTAssertEqual(calculator.getLastKnownToday(), 0)
        XCTAssertEqual(calculator.getYesterdayTotal(), 2000)
    }

    func test_afterResetForNewDayFirstUpdateRecapturesBootOffset() {
        _ = calculator.update(cumulativeSteps: 1000, yesterdayTotal: 0)
        _ = calculator.update(cumulativeSteps: 3000, yesterdayTotal: 0)
        XCTAssertEqual(calculator.getLastKnownToday(), 2000)

        calculator.resetForNewDay(yesterdayTotal: 2000)

        let result = calculator.update(cumulativeSteps: 3500, yesterdayTotal: 2000)
        XCTAssertEqual(result, 0)
    }

    func test_onDeviceRebootResetsBootOffsetAndLastKnownToZero() {
        _ = calculator.update(cumulativeSteps: 5000, yesterdayTotal: 0)
        _ = calculator.update(cumulativeSteps: 8000, yesterdayTotal: 0)
        XCTAssertEqual(calculator.getLastKnownToday(), 3000)

        let result = calculator.onDeviceReboot(cumulativeSteps: 100, yesterdayTotal: 3000)

        XCTAssertEqual(result, 0)
        XCTAssertEqual(calculator.getLastKnownToday(), 0)
        XCTAssertEqual(calculator.getYesterdayTotal(), 3000)
    }

    func test_afterOnDeviceRebootNextUpdateRecapturesBootOffset() {
        _ = calculator.update(cumulativeSteps: 5000, yesterdayTotal: 0)
        _ = calculator.update(cumulativeSteps: 8000, yesterdayTotal: 0)

        calculator.onDeviceReboot(cumulativeSteps: 100, yesterdayTotal: 3000)

        let nextUpdate = calculator.update(cumulativeSteps: 500, yesterdayTotal: 3000)
        XCTAssertEqual(nextUpdate, 0)
    }

    func test_getLastKnownTodayReturnsZeroInitially() {
        XCTAssertEqual(calculator.getLastKnownToday(), 0)
    }

    func test_getYesterdayTotalReturnsZeroInitially() {
        XCTAssertEqual(calculator.getYesterdayTotal(), 0)
    }

    func test_updateUsesYesterdayTotalParameterNotStoredValue() {
        calculator.setYesterdayTotal(999)
        _ = calculator.update(cumulativeSteps: 2000, yesterdayTotal: 100)

        let result = calculator.update(cumulativeSteps: 4000, yesterdayTotal: 100)
        XCTAssertEqual(result, 1900)
    }
}
