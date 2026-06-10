import XCTest
@testable import MoWalk

@MainActor
final class DashboardViewModelTests: XCTestCase {

    private var mockPersistence: MockPersistence!
    private var mockStepCounting: MockStepCounting!
    private var mockHealth: MockHealthData!
    private var viewModel: DashboardViewModel!

    override func setUp() async throws {
        try await super.setUp()
        mockPersistence = MockPersistence()
        mockStepCounting = MockStepCounting()
        mockHealth = MockHealthData()
    }

    override func tearDown() {
        viewModel = nil
        mockPersistence = nil
        mockStepCounting = nil
        mockHealth = nil
        super.tearDown()
    }

    func makeViewModel() -> DashboardViewModel {
        DashboardViewModel(
            persistence: mockPersistence,
            stepService: mockStepCounting,
            healthKit: mockHealth
        )
    }

    func test_initialStateHasDefaultValues() async {
        viewModel = makeViewModel()
        try? await Task.sleep(nanoseconds: 100_000_000)

        XCTAssertEqual(viewModel.todaySteps, 0)
        XCTAssertEqual(viewModel.distance, 0)
        XCTAssertEqual(viewModel.calories, 0)
        XCTAssertEqual(viewModel.dailyGoal, 8000)
        XCTAssertEqual(viewModel.progressPercent, 0)
        XCTAssertNil(viewModel.error)
        XCTAssertFalse(viewModel.isRefreshing)
        XCTAssertTrue(viewModel.isStepCountingAvailable)
    }

    func test_todayStepsEntityUpdatesState() async {
        mockPersistence.todayEntry = StepEntry(
            date: "2024-06-01",
            steps: 5000,
            distance: 2.5,
            calories: 250.0
        )
        viewModel = makeViewModel()
        try? await Task.sleep(nanoseconds: 100_000_000)

        XCTAssertEqual(viewModel.todaySteps, 5000)
        XCTAssertEqual(viewModel.distance, 2.5)
        XCTAssertEqual(viewModel.calories, 250.0)
        XCTAssertEqual(viewModel.progressPercent, 5000.0 / 8000.0)
    }

    func test_nullTodayEntityResultsInZeros() async {
        mockPersistence.todayEntry = nil
        viewModel = makeViewModel()
        try? await Task.sleep(nanoseconds: 100_000_000)

        XCTAssertEqual(viewModel.todaySteps, 0)
        XCTAssertEqual(viewModel.distance, 0)
        XCTAssertEqual(viewModel.calories, 0)
    }

    func test_userProfileOverridesDailyGoal() async {
        mockPersistence.profile = UserProfile(dailyStepGoal: 12000)
        mockPersistence.todayEntry = StepEntry(
            date: "2024-06-01",
            steps: 6000,
            distance: 3.0,
            calories: 300.0
        )
        viewModel = makeViewModel()
        try? await Task.sleep(nanoseconds: 100_000_000)

        XCTAssertEqual(viewModel.dailyGoal, 12000)
        XCTAssertEqual(viewModel.progressPercent, 0.5)
    }

    func test_progressPercentIsZeroWhenGoalIsZero() async {
        mockPersistence.profile = UserProfile(dailyStepGoal: 0)
        mockPersistence.todayEntry = StepEntry(
            date: "2024-06-01",
            steps: 5000,
            distance: 2.5,
            calories: 250.0
        )
        viewModel = makeViewModel()
        try? await Task.sleep(nanoseconds: 100_000_000)

        XCTAssertEqual(viewModel.progressPercent, 0)
    }

    func test_progressPercentReachesOneHundredWhenGoalIsMet() async {
        mockPersistence.profile = UserProfile(dailyStepGoal: 10000)
        mockPersistence.todayEntry = StepEntry(
            date: "2024-06-01",
            steps: 10000,
            distance: 5.0,
            calories: 500.0
        )
        viewModel = makeViewModel()
        try? await Task.sleep(nanoseconds: 100_000_000)

        XCTAssertEqual(viewModel.progressPercent, 1.0)
    }

    func test_progressPercentCappedAtOneWhenStepsExceedGoal() async {
        mockPersistence.profile = UserProfile(dailyStepGoal: 5000)
        mockPersistence.todayEntry = StepEntry(
            date: "2024-06-01",
            steps: 7500,
            distance: 3.75,
            calories: 375.0
        )
        viewModel = makeViewModel()
        try? await Task.sleep(nanoseconds: 100_000_000)

        XCTAssertEqual(viewModel.progressPercent, 1.0)
    }

    func test_clearErrorClearsErrorState() async {
        mockPersistence.todayEntry = nil
        viewModel = makeViewModel()
        viewModel.clearError()

        XCTAssertNil(viewModel.error)
    }
}
