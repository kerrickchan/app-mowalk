import CoreMotion
import Foundation
import UIKit

final class StepCounterService: @unchecked Sendable {
    private let pedometer = CMPedometer()
    private var persistence: StepPersistence?
    private var backgroundTaskID: UIBackgroundTaskIdentifier?
    private var isMonitoring = false

    var onStepUpdate: ((StepData) -> Void)?
    var isStepCountingAvailable: Bool {
        CMPedometer.isStepCountingAvailable()
    }

    var isPedometerAuthorized: Bool {
        CMPedometer.authorizationStatus() == .authorized
    }

    func requestAuthorization() async -> Bool {
        await withCheckedContinuation { continuation in
            pedometer.queryPedometerData(from: Date(timeIntervalSinceNow: -3600), to: Date()) { _, error in
                continuation.resume(returning: error == nil)
            }
        }
    }

    func queryTodaySteps() async throws -> Int {
        let calendar = Calendar.current
        let now = Date()
        let startOfDay = calendar.startOfDay(for: now)

        return try await withCheckedThrowingContinuation { continuation in
            pedometer.queryPedometerData(from: startOfDay, to: now) { data, error in
                if let error = error {
                    continuation.resume(throwing: error)
                    return
                }
                let steps = data?.numberOfSteps.intValue ?? 0
                continuation.resume(returning: steps)
            }
        }
    }

    func queryTodayDistance() async throws -> Double {
        let calendar = Calendar.current
        let now = Date()
        let startOfDay = calendar.startOfDay(for: now)

        return try await withCheckedThrowingContinuation { continuation in
            pedometer.queryPedometerData(from: startOfDay, to: now) { data, error in
                if let error = error {
                    continuation.resume(throwing: error)
                    return
                }
                let distance = data?.distance?.doubleValue ?? 0
                continuation.resume(returning: distance)
            }
        }
    }

    func startForegroundMonitoring(persistence: StepPersistence) {
        self.persistence = persistence
        guard !isMonitoring, isStepCountingAvailable else { return }
        isMonitoring = true

        let calendar = Calendar.current
        let now = Date()
        let startOfDay = calendar.startOfDay(for: now)

        pedometer.startUpdates(from: startOfDay) { [weak self] data, error in
            guard let self = self, let data = data, error == nil else { return }
            let stepData = StepData(
                steps: data.numberOfSteps.intValue,
                distance: data.distance?.doubleValue ?? 0,
                floorsAscended: data.floorsAscended?.intValue ?? 0
            )
            self.handleStepUpdate(stepData)
        }

        registerBackgroundTask()
    }

    func stopForegroundMonitoring() {
        isMonitoring = false
        pedometer.stopUpdates()
        endBackgroundTask()
    }

    private func handleStepUpdate(_ data: StepData) {
        onStepUpdate?(data)

        guard let persistence = persistence else { return }
        let today = Date.todayISO
        let entry = StepEntry(date: today, steps: data.steps, distance: data.distance)
        do {
            try persistence.upsert(entry)
        } catch {
            print("StepCounterService: failed to persist step data: \(error)")
        }
    }

    private func registerBackgroundTask() {
        backgroundTaskID = UIApplication.shared.beginBackgroundTask { [weak self] in
            self?.endBackgroundTask()
        }
    }

    private func endBackgroundTask() {
        if let id = backgroundTaskID {
            UIApplication.shared.endBackgroundTask(id)
            backgroundTaskID = nil
        }
    }

    deinit {
        stopForegroundMonitoring()
    }
}

struct StepData {
    let steps: Int
    let distance: Double
    let floorsAscended: Int
}
