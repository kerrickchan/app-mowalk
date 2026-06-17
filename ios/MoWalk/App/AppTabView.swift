import SwiftUI

enum AppTab: String, CaseIterable {
    case dashboard
    case trends
    case calendar
    case settings

    var icon: String {
        switch self {
        case .dashboard: "figure.walk"
        case .trends: "chart.line.uptrend.xyaxis"
        case .calendar: "calendar"
        case .settings: "gearshape"
        }
    }

    var title: String {
        switch self {
        case .dashboard: "Dashboard"
        case .trends: "Trends"
        case .calendar: "Calendar"
        case .settings: "Settings"
        }
    }
}

struct AppTabView: View {
    @State private var selectedTab: AppTab = .dashboard
    @State private var dashboardVM: DashboardViewModel
    @State private var trendsVM: TrendsViewModel
    @State private var calendarVM: CalendarViewModel
    @State private var settingsVM: SettingsViewModel

    private let persistence: PersistenceService
    private let stepService = StepCounterService()

    init(persistence: PersistenceService) {
        self.persistence = persistence
        let healthKit = HealthKitService()

        _dashboardVM = State(initialValue: DashboardViewModel(
            persistence: persistence,
            stepService: stepService,
            healthKit: healthKit
        ))
        _trendsVM = State(initialValue: TrendsViewModel(persistence: persistence))
        _calendarVM = State(initialValue: CalendarViewModel(persistence: persistence))
        _settingsVM = State(initialValue: SettingsViewModel(
            persistence: persistence,
            stepService: stepService,
            healthKit: healthKit
        ))
    }

    var body: some View {
        TabView(selection: $selectedTab) {
            DashboardView(viewModel: dashboardVM)
                .tabItem {
                    Label(AppTab.dashboard.title, systemImage: AppTab.dashboard.icon)
                }
                .tag(AppTab.dashboard)

            TrendsView(viewModel: trendsVM)
                .tabItem {
                    Label(AppTab.trends.title, systemImage: AppTab.trends.icon)
                }
                .tag(AppTab.trends)

            CalendarView(viewModel: calendarVM)
                .tabItem {
                    Label(AppTab.calendar.title, systemImage: AppTab.calendar.icon)
                }
                .tag(AppTab.calendar)

            SettingsView(viewModel: settingsVM)
                .tabItem {
                    Label(AppTab.settings.title, systemImage: AppTab.settings.icon)
                }
                .tag(AppTab.settings)
        }
        .tint(MoWalkTheme.accent)
        .onAppear {
            Task {
                let authorized = await stepService.requestAuthorization()
                if authorized {
                    stepService.startForegroundMonitoring(persistence: persistence)
                }
                try? await HealthKitService().requestAuthorization()
            }
        }
        .onChange(of: selectedTab) { _, newTab in
            if newTab == .trends {
                trendsVM.loadData()
            } else if newTab == .calendar {
                calendarVM.loadMonth()
            }
        }
    }
}
