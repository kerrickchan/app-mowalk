# MoWalk Android Implementation Plan

> **App:** MoWalk — Phone-only pedometer with Health Connect
> **Target:** `android/` folder under `/Users/kerrick/Projects/app/app-mowalk/`
> **Tech:** Kotlin, Jetpack Compose + Material 3, MVVM, Room, Health Connect SDK 1.2+, WorkManager, MPAndroidChart
> **Min SDK:** 26 (Android 8.0) | **Target SDK:** 34

---

## Batch 0 — Project Root Gradle Configuration

These files scaffold the Gradle build system before any Android module code exists.

### 0.1 `android/settings.gradle.kts`
Root-level Gradle settings. Declares the Android app module, includes the Compose BOM platform, configures the `libs` version catalog, registers the Kotlin and Android Gradle plugins via the plugins block, and enables the KSP plugin alias for Room annotation processing.

### 0.2 `android/build.gradle.kts`
Project-level build script. Declares the Android application plugin version, Kotlin version, and KSP version in the `plugins` block. No module-level dependencies live here.

### 0.3 `android/gradle.properties`
Standard Android project properties: JVM args (`-Xmx2048m`), AndroidX enabled, non-transitive R classes, Kotlin code style `official`, and org-wide settings.

### 0.4 `android/gradle/wrapper/gradle-wrapper.properties`
Gradle wrapper properties specifying the distribution URL (Gradle 8.9 or later), checksum, and wrapper jar URL.

### 0.5 `android/.gitignore`
Android-specific gitignore entries: `build/`, `*.apks`, `*.jks`, local.properties, `.idea/`, `*.iml`, `gradle.properties.example`, etc.

---

## Batch 1 — App-Level Gradle, Manifest, and Resources

These files define the app's build configuration, entry point, and all static resources.

### 1.1 `android/app/build.gradle.kts`
App-level build script. Configures `compileSdk = 34`, `defaultConfig` with `minSdk = 26`, `targetSdk = 34`, application id `com.mowalk.app`, version code 1, version name "1.0.0". Declares `buildFeatures { compose = true }`, `ksp` plugin alias, and all runtime dependencies:
- Compose BOM + ui, material3, ui-tooling-preview, ui-tooling (debug)
- Navigation compose 2.8.4
- Room runtime 2.6.1, room-ktx 2.6.1, room-compiler (ksp) 2.6.1
- Health Connect client 1.2.0
- WorkManager runtime-ktx 2.9.0
- MPAndroidChart v3.1.0
- Coroutines android 1.8.1
- Lifecycle runtime-compose 2.8.7, lifecycle-viewmodel-compose 2.8.7
- JUnit 4.13.2 (test), kotlinx-coroutines-test 1.8.1 (test), room-testing 2.6.1 (androidTest), ui-test-junit4 (androidTest)
- `isCoreLibraryDesugaringEnabled = true` for `java.time` backport on API < 26
- Desugaring dependency `com.android.tools:desugar_jdk_libs:2.0.4`

### 1.2 `android/app/proguard-rules.pro`
R8/ProGuard rules. Preserves `androidx.health.connect.client.**` classes and members from obfuscation. Keeps Room entity classes and DAO interfaces. No other special rules needed at MVP stage.

### 1.3 `android/app/src/main/AndroidManifest.xml`
Declares:
- Permissions: `ACTIVITY_RECOGNITION`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_HEALTH`, `FOREGROUND_SERVICE_DATA_SYNC`, `POST_NOTIFICATIONS`, plus Health Connect `uses-permission` for `health.READ_STEPS` and `health.WRITE_STEPS` (scoped permissions model).
- `<uses-feature android:name="android.hardware.sensor.accelerometer" android:required="false" />`
- `<application>` with `android:allowBackup="false"`, `android:icon`, `android:label`, `android:theme="@style/Theme.MoWalk"`, `android:exported="false"` (default).
- `<activity>` for `MainActivity` with `android:exported="true"`, intent-filter for `MAIN` action + `LAUNCHER` category, and `android:theme="@style/Theme.MoWalk"`.
- `<service>` for `StepCounterService` with `android:foregroundServiceType="health|dataSync"` and `android:exported="false"`.
- Activity-alias for Health Connect permission usage display.

### 1.4 `android/app/src/main/res/values/strings.xml`
All string resources: app name "MoWalk", notification channel name/description, step count labels (今日步数, 步, km, kcal), settings labels, Health Connect messages, permission rationale text, error messages, CSV export messages, empty state text.

### 1.5 `android/app/src/main/res/values/colors.xml`
Material 3 color system definitions: `primary`, `secondary`, `tertiary`, `background`, `surface`, `error` palettes for both light and dark variants (using `color-m3-light-*` / `color-m3-dark-*` naming for M3 dynamic colors), plus `step_count_green` accent for the step circle.

### 1.6 `android/app/src/main/res/values/themes.xml`
App theme declaration: `<style name="Theme.MoWalk" parent="android:Theme.Material.Light.NoActionBar">` with `windowBackground`, `colorPrimary`, `colorOnPrimary`, `android:statusBarColor`, `android:navigationBarColor`.

### 1.7 `android/app/src/main/res/values-night/themes.xml`
Dark variant of the app theme with dark surface/background colors and adjusted status bar contrast.

### 1.8 `android/app/src/main/res/drawable/ic_launcher_foreground.xml`
Vector drawable for the adaptive icon foreground (step/shoe icon or abstract "M" monogram).

### 1.9 `android/app/src/main/res/drawable/ic_launcher_background.xml`
Solid color or gradient drawable for the adaptive icon background.

### 1.10 `android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
Adaptive icon launcher reference pointing to the foreground/background drawables.

### 1.11 `android/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
Round adaptive icon launcher reference.

### 1.12 `android/app/src/main/res/xml/backup_rules.xml`
`fullBackupContent` set to `false` (no cloud backup of health data).

### 1.13 `android/app/src/main/res/xml/data_extraction_rules.xml`
Data extraction rules excluding health data from cloud backup.

---

## Batch 2 — Theme, Navigation, and Shared UI Components

Foundational UI building blocks used by all screens.

### 2.1 `android/app/src/main/java/com/mowalk/app/ui/theme/Color.kt`
M3 color definitions as `@Immutable` color constants. Defines light and dark color schemes: `primaryLight`, `onPrimaryLight`, `primaryContainerLight`, `secondaryLight`, `tertiaryLight`, `backgroundLight`, `surfaceLight`, `errorLight`, plus their dark counterparts. Also defines `StepGreen` (#4CAF50 or similar) and `StepGreenDark` for the step progress circle.

### 2.2 `android/app/src/main/java/com/mowalk/app/ui/theme/Type.kt`
Typography setup. Uses `MaterialTheme.typography` defaults with optional custom `displayTextLarge` or `headlineLarge` overrides for the step count display. Defines a `MoWalkTypography()` composable that returns a customized `Typography` instance.

### 2.3 `android/app/src/main/java/com/mowalk/app/ui/theme/Theme.kt`
`MoWalkTheme` composable: wraps `MaterialTheme`, sets up `dynamicColor = true` on Android 12+, provides `isDarkTheme` parameter (defaults to `FollowSystem`), configures `colorScheme` based on light/dark mode, and wraps content in `SystemUiController` for status bar styling. Exposes `currentColorScheme` via composition local for chart theming.

### 2.4 `android/app/src/main/java/com/mowalk/app/ui/navigation/AppNavigation.kt`
Jetpack Compose Navigation setup. Defines `NavRoute` sealed interface/object with routes: `Dashboard`, `Trends`, `Calendar`, `Settings`. Creates `AppNavHost()` composable with `NavHost` pointing to `NavRoute.Dashboard`. Registers each destination with its screen composable and ViewModel (via `hiltViewModel` or manual factory). Configures back stack behavior.

### 2.5 `android/app/src/main/java/com/mowalk/app/ui/components/StepCircle.kt`
Reusable circular progress indicator showing today's steps vs. daily goal. Takes `currentSteps`, `goal`, and optional `size` parameters. Draws an arc using `Canvas` or `ProgressBar`-style composable. Shows step count text in the center. Color transitions from green (under goal) to amber (at goal). Animated arc sweep.

### 2.6 `android/app/src/main/java/com/mowalk/app/ui/components/StatRow.kt`
Horizontal row displaying a metric label + value + unit (e.g., "5.2 km", "310 kcal"). Uses `Row` with `Text` composables. Accepts an icon parameter (small vector icon before the label).

### 2.7 `android/app/src/main/java/com/mowalk/app/ui/components/ProgressCard.kt`
Card component wrapping the step goal progress bar. Shows a filled progress segment up to percentage of goal, with a label like "65% of 8,000 goal". Uses `LinearProgressIndicator` inside a `Card`.

### 2.8 `android/app/src/main/java/com/mowalk/app/ui/components/EmptyStateCard.kt`
Reusable empty-state placeholder shown when no step data is available yet. Displays an icon, a message like "Start walking to see your steps", and a subtle animation.

### 2.9 `android/app/src/main/java/com/mowalk/app/ui/components/PullToRefresh.kt`
Wraps screen content in `PullToRefreshBox` for manual refresh triggering. Used on Dashboard and Trends screens.

---

## Batch 3 — Data Layer: Room Database

Local persistence layer with entities, DAOs, database, and migrations.

### 3.1 `android/app/src/main/java/com/mowalk/app/data/local/DailyStepEntity.kt`
Room `@Entity` class. Columns: `date` (TEXT, PRIMARY KEY, format `yyyy-MM-dd`), `steps` (INTEGER, NOT NULL DEFAULT 0), `distance` (REAL, NOT NULL DEFAULT 0.0, meters), `calories` (REAL, NOT NULL DEFAULT 0.0, kcal), `isManuallyEdited` (INTEGER, NOT NULL DEFAULT 0, boolean as int). Companion object with `today()` factory for creating today's entity.

### 3.2 `android/app/src/main/java/com/mowalk/app/data/local/UserProfileEntity.kt`
Room `@Entity` class. Columns: `id` (INTEGER, PRIMARY KEY, always 1 — singleton), `height` (REAL, NULLABLE, cm), `weight` (REAL, NULLABLE, kg), `dailyStepGoal` (INTEGER, NOT NULL DEFAULT 8000), `hcSyncEnabled` (INTEGER, NOT NULL DEFAULT 1, boolean as int). Companion object with `default()` factory.

### 3.3 `android/app/src/main/java/com/mowalk/app/data/local/StepDao.kt`
Room `@Dao` interface. Methods:
- `suspend fun getByDate(date: String): DailyStepEntity?`
- `fun observeByDate(date: String): Flow<DailyStepEntity?>`
- `fun observeRange(start: String, end: String): Flow<List<DailyStepEntity>>`
- `suspend fun sumSteps(start: String, end: String): Int?`
- `suspend fun upsert(entity: DailyStepEntity)` (using `@Insert(onConflict = OnConflictStrategy.REPLACE)`)
- `suspend fun deleteAll()`
- `suspend fun getUserProfile(): UserProfileEntity?`
- `fun observeUserProfile(): Flow<UserProfileEntity?>`
- `suspend fun upsertProfile(profile: UserProfileEntity)`

### 3.4 `android/app/src/main/java/com/mowalk/app/data/local/MoWalkDatabase.kt`
`@Database` abstract class. Entities: `DailyStepEntity`, `UserProfileEntity`. Version: 1. Exposes `StepDao` and `UserProfileDao` (or combined DAO) as abstract methods. Static `instance` holder using `Room.databaseBuilder` with `fallbackToDestructiveMigration(false)`. Provides `addMigrations()` method returning the list of `Migration` objects for future version bumps.

### 3.5 `android/app/src/main/java/com/mowalk/app/data/local/Migrations.kt`
`Migration` object / companion with `VERSION_1` constant = 1. Defines `ALL_MIGRATIONS` list (currently empty since this is v1). Sets up the pattern for future `Migration(1, 2)` objects when schema evolves.

### 3.6 `android/app/src/main/java/com/mowalk/app/data/local/LocalDataSource.kt`
Repository-facing data source wrapping `StepDao`. Provides `getTodaySteps()`, `getStepsRange(start, end)`, `upsertTodaySteps(steps, distance, calories)`, `getUserProfile()`, `updateUserProfile(...)`, `clearAllData()`, and `observeTodaySteps(): Flow<DailyStepEntity?>`. Abstracts Room details from the repository layer.

---

## Batch 4 — Data Layer: Sensor and Health Connect Data Sources

Hardware and platform data sources.

### 4.1 `android/app/src/main/java/com/mowalk/app/data/sensor/SensorDataSource.kt`
Class wrapping `SensorManager`. Constructor takes `Context`. Fields: `sensorManager`, `stepCounterSensor` (TYPE_STEP_COUNTER), `accelerometerSensor` (TYPE_ACCELEROMETER). Properties: `isStepCounterAvailable` (boolean), `isAccelerometerAvailable`. Method `observeSteps(): Flow<Int>` — registers `SensorEventListener` and emits cumulative step counts. Method `observeAccelSteps(): Flow<Int>` — simple peak-detection fallback using accelerometer. Method `unregisterListener(SensorEventListener)` for cleanup. Thread-safe with coroutines `Channel<Int>` bridging sensor callbacks to Flow.

### 4.2 `android/app/src/main/java/com/mowalk/app/data/sensor/StepDeltaCalculator.kt`
Pure logic class for computing today's steps from cumulative sensor readings. State: `bootOffset` (Int, initially UNKNOWN), `previousDayTotal` (Int, loaded from Room). Method `update(cumulativeSteps: Int, yesterdayTotal: Int): Int` — computes `rawToday = cumulative - bootOffset - yesterdayTotal`, handles boot-offset capture on first call, returns `max(rawToday, lastKnownToday)` to prevent negative/decreasing steps. Method `reset()` for midnight rollover. Method `onDeviceReboot()` for `BOOT_COMPLETED` receiver.

### 4.3 `android/app/src/main/java/com/mowalk/app/data/healthconnect/HealthConnectPermissionHelper.kt`
Utility for managing Health Connect permission state. Methods: `isPermissionGranted(context, permissions)`, `createPermissionRequestIntent(context, permissions)`, `isHealthConnectAvailable(context)` (checks `HealthConnectClient.getSdkStatus()`), `openHealthConnectInstallIntent(context)` (opens Play Store). Also handles the `ACTIVITY_RECOGNITION` runtime permission request via `ActivityResultContracts.RequestPermission()`.

### 4.4 `android/app/src/main/java/com/mowalk/app/data/healthconnect/HealthConnectDataSource.kt`
Class wrapping `HealthConnectClient`. Constructor takes `Context`. Property `isAvailable` from SDK status check. Methods:
- `suspend fun readStepsRecords(startDate: LocalDate, endDate: LocalDate): List<StepsRecord>` — reads aggregated steps from HC.
- `suspend fun readDistanceRecords(startDate: LocalDate, endDate: LocalDate): List<DistanceRecord>` — reads distance.
- `suspend fun readCaloriesRecords(startDate: LocalDate, endDate: LocalDate): List<TotalCaloriesBurnedRecord>` — reads calories.
- `suspend fun writeStepsRecord(date: LocalDate, steps: Int, distanceMeters: Float, caloriesKcal: Float)` — writes aggregated record to HC.
- `suspend fun aggregateRecords(records: List<StepsRecord>): DailyStepAggregation` — sums counts, computes distance and calories from user profile.
- `suspend fun syncDateRange(startDate: LocalDate, endDate: LocalDate): SyncResult` — orchestrates full bidirectional sync for a date range.

---

## Batch 5 — Data Layer: Repository

Aggregation and conflict resolution.

### 5.1 `android/app/src/main/java/com/mowalk/app/data/repository/ConflictResolver.kt`
Pure logic for merging Health Connect data with local Room data. Method `resolveConflict(local: DailyStepEntity?, hcSteps: Int): DailyStepEntity` — if local exists AND `isManuallyEdited`, return local unchanged. Otherwise return entity with `steps = max(local?.steps ?: 0, hcSteps)`. Method `resolveDistanceCalories(local: DailyStepEntity?, hcDistance: Float, hcCalories: Float): Pair<Float, Float>` — takes max of distance/calories.

### 5.2 `android/app/src/main/java/com/mowalk/app/data/repository/StepRepository.kt`
Central repository aggregating all data sources. Constructor injects `LocalDataSource`, `SensorDataSource`, `HealthConnectDataSource`, and `ConflictResolver`. Properties/methods:
- `val isStepCounterAvailable: Boolean`
- `val hcSyncEnabled: StateFlow<Boolean>`
- `fun observeTodaySteps(): Flow<DailyStepEntity?>` — combines sensor live data with Room persistence.
- `fun observeStepsRange(start: String, end: String): Flow<List<DailyStepEntity>>`
- `suspend fun upsertTodaySteps(steps: Int, distance: Float, calories: Float)`
- `suspend fun updateManualEntry(date: String, steps: Int)` — sets `isManuallyEdited = true`.
- `suspend fun syncWithHealthConnect()` — orchestrates bidirectional sync: read HC for range, resolve conflicts, upsert to Room, then write new local records to HC.
- `suspend fun clearAllData()` — deletes all Room data, clears HC records for app.
- `suspend fun getUserProfile(): UserProfileEntity?`
- `suspend fun updateUserProfile(height: Float?, weight: Float?, dailyStepGoal: Int)`
- `suspend fun setHcSyncEnabled(enabled: Boolean)` — toggles `hcSyncEnabled` and triggers one-shot sync if enabling.
- `fun getWeeklyData(): Flow<List<DailyStepEntity>>` — convenience for trends.
- `fun getMonthlyData(year: Int, month: Int): Flow<List<DailyStepEntity>>`

---

## Batch 6 — Service Layer: Foreground Service

Background step counting service.

### 6.1 `android/app/src/main/java/com/mowalk/app/service/StepCounterService.kt`
`Service` (not `ForegroundService` subclass — uses `startForeground()` manually for broader compatibility). Key responsibilities:
- `onCreate()`: initializes `SensorDataSource`, `StepDeltaCalculator`, `LocalDataSource` via `ServiceLocator`. Creates notification channel `step_channel` (IMPORTANCE_LOW).
- `onStartCommand()`: calls `startForeground()` with persistent notification, registers sensor listener, starts delta calculation coroutine. Handles `EXTRA_STOP` intent extra to call `stopSelf()`. Returns `START_STICKY`.
- Sensor callback `onSensorChanged()`: passes cumulative steps to `StepDeltaCalculator`, computes today's delta, calls `LocalDataSource.upsertTodaySteps()`.
- `onDestroy()`: unregisters sensor listener, cancels coroutines.
- Notification: title "步数记录中", icon step icon, action button "停止" that sends stop intent.
- `BOOT_COMPLETED` receiver integration: on boot, updates `bootOffset` and recalculates today's delta from last-saved Room value.

### 6.2 `android/app/src/main/java/com/mowalk/app/service/BootReceiver.kt`
`BroadcastReceiver` for `android.intent.action.BOOT_COMPLETED`. On receipt: starts `StepCounterService` if it was running before reboot, updates `bootOffset` via `StepDeltaCalculator`, and re-persists today's step count from Room.

---

## Batch 7 — ViewModel Layer

UI state holders for each screen.

### 7.1 `android/app/src/main/java/com/mowalk/app/ui/dashboard/DashboardState.kt`
Data class holding dashboard UI state: `todaySteps: Int`, `distance: Float`, `calories: Float`, `dailyGoal: Int`, `progressPercent: Float`, `isStepCounterAvailable: Boolean`, `isRefreshing: Boolean`, `hcSyncAvailable: Boolean`, `error: String?`. Companion object with `copy` helpers for partial updates.

### 7.2 `android/app/src/main/java/com/mowalk/app/ui/dashboard/DashboardViewModel.kt`
`ViewModel` with `SavedStateHandle`. Injects `StepRepository`. State: `val state: StateFlow<DashboardState>`. Init block: loads today's steps from repository, loads user profile for goal, observes sensor updates. Methods:
- `fun refresh()` — triggers sensor re-read + manual HC sync.
- `fun onRefreshRequested()` — sets `isRefreshing = true`, calls `refresh()`, sets `isRefreshing = false` on completion.
- `fun openSettings()` — navigation callback (exposed to UI).
- `fun openTrends()` — navigation callback.
- `fun openCalendar()` — navigation callback.

### 7.3 `android/app/src/main/java/com/mowalk/app/ui/trends/TrendsState.kt`
Data class: `dataPoints: List<DailyStepEntity>`, `period: TrendsPeriod` (enum: `Week`, `Month`), `isLoading: Boolean`, `selectedDay: DailyStepEntity?`.

### 7.4 `android/app/src/main/java/com/mowalk/app/ui/trends/TrendsViewModel.kt`
`ViewModel`. Injects `StepRepository`. State: `StateFlow<TrendsState>`. Init: loads weekly data by default. Methods:
- `fun setPeriod(period: TrendsPeriod)` — switches between Week/Month, reloads data.
- `fun onSelectDay(day: DailyStepEntity)` — sets selected day for detail bottom sheet.
- `fun onDismissDetail()` — clears selected day.

### 7.5 `android/app/src/main/java/com/mowalk/app/ui/calendar/CalendarState.kt`
Data class: `monthDays: List<CalendarDay>`, `selectedDate: String?`, `isLoading: Boolean`, `currentMonth: YearMonth`.

### 7.6 `android/app/src/main/java/com/mowalk/app/ui/calendar/CalendarViewModel.kt`
`ViewModel`. Injects `StepRepository`. State: `StateFlow<CalendarState>`. Init: loads current month's data. Methods:
- `fun swipeToPreviousMonth()` / `fun swipeToNextMonth()` — navigates months, loads data for new range.
- `fun onSelectDay(date: String)` — navigation callback to view that day.
- `fun getStepCountForDate(date: String): Int?` — lookup helper for heat map coloring.

### 7.7 `android/app/src/main/java/com/mowalk/app/ui/settings/SettingsState.kt`
Data class: `height: Float?`, `weight: Float?`, `dailyStepGoal: Int`, `hcSyncEnabled: Boolean`, `hcAvailable: Boolean`, `stepCounterAvailable: Boolean`, `versionName: String`.

### 7.8 `android/app/src/main/java/com/mowalk/app/ui/settings/SettingsViewModel.kt`
`ViewModel`. Injects `StepRepository`. State: `StateFlow<SettingsState>`. Init: loads user profile and availability flags. Methods:
- `fun updateHeight(cm: Float)` / `fun updateWeight(kg: Float)` / `fun updateDailyGoal(goal: Int)` — validates and saves.
- `fun toggleHcSync(enabled: Boolean)` — toggles sync, triggers one-shot sync on enable.
- `fun clearAllData()` — triggers confirmation dialog (UI handles dialog state).
- `fun exportCsv()` — delegates to `CsvExporter`.

---

## Batch 8 — UI Layer: Screens

Jetpack Compose screen implementations.

### 8.1 `android/app/src/main/java/com/mowalk/app/ui/dashboard/DashboardScreen.kt`
Compose `@Composable` screen. Layout: `Scaffold` with top app bar ("MoWalk" title + settings icon). Main content: `PullToRefreshBox` wrapping `Column`. Center: `StepCircle` with today's step count. Below: `StatRow` components for distance and calories. Below: `ProgressCard` showing goal progress. Bottom: clickable cards for "This Week Trend" and "Calendar" that trigger navigation callbacks. Warning banner if `!isStepCounterAvailable`. Snackbar for error messages.

### 8.2 `android/app/src/main/java/com/mowalk/app/ui/trends/TrendsScreen.kt`
Compose `@Composable` screen. Top: `SegmentedButton` for "本周" / "本月" toggle. Middle: `AndroidView` hosting MPAndroidChart `LineChart` with dataset from ViewModel. Chart shows steps per day with grid lines, axis labels, and touch interaction. Bottom sheet (when `selectedDay` is set): shows detailed steps, distance, calories for the selected date. Pull-to-refresh support.

### 8.3 `android/app/src/main/java/com/mowalk/app/ui/calendar/CalendarScreen.kt`
Compose `@Composable` screen. Horizontal pager of month grids. Each month grid: 7-column layout (Sun-Sat or Mon-Sun). Each cell: day number with mini step count. Cell background color intensity scales with step count (heat map: light green → dark green). Selected day highlighted with ring. Header shows month/year with prev/next arrow buttons. Tap a day navigates to trend detail.

### 8.4 `android/app/src/main/java/com/mowalk/app/ui/settings/SettingsScreen.kt`
Compose `@Composable` screen. `Scaffold` with top app bar ("Settings"). Content sections:
- **Profile section:** `TextField` for height (cm), weight (kg), daily step goal — with validation and save button.
- **Health Connect section:** toggle switch for sync, info text if HC unavailable with "Install" button → Play Store.
- **Export section:** "Export CSV" button → triggers `CsvExporter`.
- **Danger zone:** "Clear All Data" red button → confirmation dialog.
- **About section:** version number, open-source licenses text.
- Battery optimization prompt card with "Go to Settings" button.

### 8.5 `android/app/src/main/java/com/mowalk/app/ui/main/MainActivity.kt`
`ComponentActivity` (or `AppCompatActivity`). `onCreate()`: sets content to `MoWalkTheme { AppNavHost() }`. Handles permission checks on first launch (ACTIVITY_RECOGNITION). Starts `StepCounterService` if not already running. Overrides `onResume()` to ensure service is running.

---

## Batch 9 — Work Manager, Export, and DI

Background work, data export, and dependency injection.

### 9.1 `android/app/src/main/java/com/mowalk/app/work/HealthConnectSyncWorker.kt`
`CoroutineWorker` subclass. `doWork()`: checks `repository.isHcSyncEnabled()` — if false, returns `Result.success()`. Otherwise calls `repository.syncWithHealthConnect()`. On success: `Result.success()`. On failure with `runAttemptCount < 3`: `Result.retry()` (WorkManager handles exponential backoff). On final failure: `Result.failure()`. Configured as periodic work with 6-hour interval (minimum allowed by WorkManager, effective sync rate depends on Doze/battery optimization).

### 9.2 `android/app/src/main/java/com/mowalk/app/work/WorkManagerInitializer.kt`
Utility object that configures WorkManager periodic work request. Creates `PeriodicWorkRequestBuilder<HealthConnectSyncWorker>` with `PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS` or 6 hours. Sets backoff policy to `EXPONENTIAL`. Sets constraints: `NEED_UNMETERED_NETWORK` + `NOT_ROAMING`. Applies unique work name `"hc_sync_periodic"`. Also provides `requestManualSync()` for one-shot sync when user toggles HC sync on.

### 9.3 `android/app/src/main/java/com/mowalk/app/export/CsvExporter.kt`
Utility class for CSV export. Method `suspend fun export(context: Context, repository: StepRepository): Boolean` — queries all `DailyStepEntity` rows ordered by date. Formats as CSV with header row `date,steps,distance,calories,isManuallyEdited`. Uses `ActivityResultContracts.CreateDocument("text/csv")` to trigger SAF file picker. Writes to `contentResolver.openOutputStream(uri)` with UTF-8 encoding. Returns success/failure. Shows Snackbar to user on completion.

### 9.4 `android/app/src/main/java/com/mowalk/app/di/ServiceLocator.kt`
Manual DI container (no framework). `object ServiceLocator` with `init` block that initializes all singletons. Provides:
- `val database: MoWalkDatabase` — Room database instance (lazy-initialized via `applicationContext`).
- `val stepDao: StepDao` — from `database.stepDao()`.
- `val localDataSource: LocalDataSource` — wraps `stepDao`.
- `val sensorDataSource: SensorDataSource` — wraps `SensorManager` from `applicationContext`.
- `val healthConnectDataSource: HealthConnectDataSource` — wraps `HealthConnectClient` from `applicationContext`.
- `val conflictResolver: ConflictResolver` — singleton stateless object.
- `val repository: StepRepository` — aggregates all above.
- `fun getApplication(): Application` — holds reference to app instance, set by `MoWalkApplication`.
Thread-safe via `by lazy` delegates.

### 9.5 `android/app/src/main/java/com/mowalk/app/MoWalkApplication.kt`
`Application` subclass. `onCreate()`: calls `ServiceLocator.initialize(this)`, initializes WorkManager via `WorkManager.initialize(this, Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).build())`, registers `HealthConnectSyncWorker` via `WorkManagerInitializer`. Ensures `StepCounterService` is started if step data exists.

---

## File Summary by Batch

| Batch | Files | Purpose |
|-------|-------|---------|
| 0 | 5 | Gradle build system scaffold |
| 1 | 13 | App build, manifest, resources, icons |
| 2 | 9 | Theme, navigation, shared components |
| 3 | 6 | Room entities, DAOs, database, migrations |
| 4 | 4 | Sensor and Health Connect data sources |
| 5 | 2 | Repository + conflict resolver |
| 6 | 2 | Foreground service + boot receiver |
| 7 | 8 | ViewModels + state data classes |
| 8 | 5 | Compose screens + MainActivity |
| 9 | 5 | WorkManager worker, CSV export, DI, Application |
| **Total** | **59 files** | |

---

## MVVM Flow Verification

Every data flow follows this path:

```
UI Screen (Compose @Composable)
  ↓ observes
ViewModel (StateFlow<DashboardState>)
  ↓ calls
StepRepository
  ↓ delegates to
  ├── LocalDataSource → Room Database (DailyStepEntity, UserProfileEntity)
  ├── SensorDataSource → SensorManager.TYPE_STEP_COUNTER → StepDeltaCalculator
  └── HealthConnectDataSource → Health Connect SDK (bidirectional)
  ↑ notifies
ForegroundService (StepCounterService) writes to Room on sensor callback
  ↑ triggered by
WorkManager (HealthConnectSyncWorker) periodic sync
```

---

## Implementation Order (Recommended)

1. **Batch 0** — Gradle scaffold (build must compile before anything else)
2. **Batch 1** — App build, manifest, resources (app must have a manifest)
3. **Batch 3** — Room database (data foundation, no UI dependencies)
4. **Batch 4** — Sensor + Health Connect data sources (data sources, no UI)
5. **Batch 5** — Repository (aggregates data sources)
6. **Batch 9** — DI + Application + WorkManager (wires everything together)
7. **Batch 2** — Theme + navigation + components (UI foundation)
8. **Batch 7** — ViewModels (connects UI to repository)
9. **Batch 8** — Screens + MainActivity (user-facing UI)
10. **Batch 6** — Foreground service + boot receiver (background infrastructure)

Batches 2, 7, 8 can be developed in parallel once the data layer (Batches 3-5, 9) is stable. Batch 6 (service) can be developed in parallel with the data layer since it only depends on the data source classes.

---

## Key Implementation Notes

- **Desugaring:** `java.time` API (LocalDate, YearMonth) requires core library desugaring for API < 26. Enable `isCoreLibraryDesugaringEnabled = true` in build.gradle.kts and add the desugaring dependency.
- **Health Connect scoped permissions:** On API 34+, Health Connect uses scoped permissions (`health.READ_STEPS`, `health.WRITE_STILLS`) declared in the manifest, but runtime permission is requested via the Health Connect `PermissionController` API, not the standard Android permission system.
- **Foreground service types:** API 34+ requires `FOREGROUND_SERVICE_HEALTH` and/or `FOREGROUND_SERVICE_DATA_SYNC` permissions in the manifest, and the service must declare `android:foregroundServiceType="health|dataSync"`.
- **MPAndroidChart Compose interop:** Use `AndroidView(factory = { ctx -> LineChart(ctx) })` inside Compose. Update chart data via `chart.data` and `chart.notifyDataSetChanged()` on the main thread.
- **Step delta never decreases:** The `StepDeltaCalculator` uses `max(rawToday, lastSavedToday)` to prevent step counts from going backward on sensor resets or midnight rollover edge cases.
- **No `fallbackToDestructiveMigration`:** Room is configured with `fallbackToDestructiveMigration(false)` to ensure historical step data survives app upgrades. Migration objects must be provided for every version bump.
- **Manual DI:** `ServiceLocator` is a Kotlin `object` with `by lazy` singletons. The `Application` class calls `ServiceLocator.initialize(application)` in `onCreate()`. No Hilt, Dagger, or Koin — keeps the dependency footprint minimal as specified.
- **Boot recovery:** On `BOOT_COMPLETED`, the `BootReceiver` must update the `bootOffset` in `StepDeltaCalculator` to the new cumulative sensor value and recalculate today's delta from the last-saved Room entry (yesterday's total).
