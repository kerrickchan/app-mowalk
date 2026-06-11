# MoWalk

A phone-only pedometer app for Android and iOS. Tracks daily steps using built-in sensors, stores data locally, and optionally syncs with Health Connect (Android) or HealthKit (iOS). No wearables required. No cloud.

## Features

- **Step counting** — uses device's built-in `TYPE_STEP_COUNTER` (Android) or HealthKit/motion sensors (iOS)
- **Daily dashboard** — steps, distance, calories, and daily goal progress
- **Trends** — weekly and monthly step history with line charts
- **Calendar view** — monthly heat map of step counts
- **CSV export** — download step history to your device
- **Health sync** — optional two-way sync with Health Connect (Android) or HealthKit (iOS)
- **Local-first** — all data stored on-device, no cloud upload
- **Dark mode** — Material Design 3 dynamic colors (Android), native SwiftUI dark theme (iOS)

## Platforms

| Platform | Min Version | Language | UI Framework |
|----------|-------------|----------|--------------|
| Android  | API 26 (8.0) | Kotlin | Jetpack Compose + Material 3 |
| iOS      | 17.0 | Swift | SwiftUI + SwiftData |

## Tech Stack

### Android
- **UI:** Jetpack Compose, Material Design 3, Navigation Compose
- **Architecture:** MVVM
- **Storage:** Room (SQLite)
- **Sensors:** `SensorManager.TYPE_STEP_COUNTER`
- **Health:** Health Connect SDK 1.2+
- **Background:** Foreground Service, WorkManager
- **DI:** Manual (lightweight ServiceLocator)

### iOS
- **UI:** SwiftUI
- **Storage:** SwiftData
- **Health:** HealthKit
- **Architecture:** MVVM with protocol-based services

## Project Structure

```
app-mowalk/
├── android/
│   ├── app/src/main/java/com/mowalk/app/
│   │   ├── data/
│   │   │   ├── local/        # Room entities, DAOs, database
│   │   │   ├── sensor/       # SensorDataSource, StepDeltaCalculator
│   │   │   └── repository/   # StepRepository
│   │   ├── di/               # ServiceLocator, ViewModelFactory
│   │   ├── service/          # StepCounterService, BootReceiver
│   │   ├── export/           # CsvExporter
│   │   ├── ui/
│   │   │   ├── main/         # MainActivity
│   │   │   ├── dashboard/    # DashboardScreen, DashboardViewModel
│   │   │   ├── trends/       # TrendsScreen, TrendsViewModel
│   │   │   ├── calendar/     # CalendarScreen, CalendarViewModel
│   │   │   ├── settings/     # SettingsScreen, SettingsViewModel
│   │   │   ├── components/   # Shared composables
│   │   │   ├── theme/        # Colors, typography, theme
│   │   │   └── navigation/   # App navigation graph
│   │   └── ...
│   └── build.gradle.kts
├── ios/
│   ├── MoWalk/
│   │   ├── App/              # App entry point, tab view
│   │   ├── Models/           # ViewModels, data models
│   │   ├── Services/         # HealthKit, persistence, step counter
│   │   └── Views/            # SwiftUI screens and components
│   └── MoWalkTests/          # Unit tests
└── docs/
    ├── PRD.md                # Product requirements
    ├── DESIGN.md             # Design workflow
    └── TECHNICAL_DESIGN.md   # Technical architecture
```

## Getting Started

### Android

**Requirements:** Android Studio Hedgehog+, JDK 17+, Android SDK 26–34

1. Open `android/` in Android Studio
2. Sync Gradle files
3. Run on a device with `TYPE_STEP_COUNTER` (most phones) and Health Connect installed

```bash
cd android
./gradlew assembleDebug
```

### iOS

**Requirements:** Xcode 16+, iOS 17+ simulator or device

1. Open `ios/` in Xcode (uses `project.yml` for configuration)
2. Resolve dependencies
3. Build and run

```bash
cd ios
# If using tuist:
tuist install
tuist generate

# Or open directly:
open project.yml
```

## Architecture

```
┌─────────────────────────────────────────┐
│              UI Layer                   │
│  Dashboard · Trends · Calendar · Settings│
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│           ViewModel Layer               │
│  State management · UI logic            │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Repository Layer                │
│  StepRepository — merges data sources   │
└────┬──────────┬──────────┬──────────────┘
     │          │          │
┌────▼───┐ ┌───▼────┐ ┌───▼──────────┐
│  Local │ │ Sensor │ │  Health Sync  │
│  (Room │ │ (Phone │ │  (HC/HealthKit│
│  DB)   │ │  HW)   │ │   SDK)       │
└────────┘ └────────┘ └──────────────┘
```

All data is stored locally on-device. Health Connect / HealthKit sync is optional and can be toggled off in Settings.

## Status

**MVP** — Core step counting, dashboard, trends, calendar, and CSV export are implemented for both platforms. Health sync is available on Android (Health Connect) and iOS (HealthKit).

## Docs

- [Product Requirements (PRD)](docs/PRD.md)
- [Technical Design](docs/TECHNICAL_DESIGN.md)
- [Design Workflow](docs/DESIGN.md)
