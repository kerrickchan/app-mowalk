import CoreMotion
import Foundation

final class StepCounterService: @unchecked Sendable {
    private let pedometer = CMPedometer()
    private let deltaCalculator = StepDeltaCalculator()

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

    func startUpdates() -> AsyncStream<StepData> {
        AsyncStream { continuation in
            let now = Date()
            let calendar = Calendar.current
            let startOfDay = calendar.startOfDay(for: now)

            pedometer.startUpdates(from: startOfDay) { data, error in
                guard let data = data, error == nil else { return }
                let stepData = StepData(
                    steps: data.numberOfSteps.intValue,
                    distance: data.distance?.doubleValue ?? 0,
                    floorsAscended: data.floorsAscended?.intValue ?? 0
                )
                continuation.yield(stepData)
            }

            continuation.onTermination = { [weak self] _ in
                self?.stopUpdates()
            }
        }
    }

    func stopUpdates() {
        pedometer.stopUpdates()
    }
}

struct StepData {
    let steps: Int
    let distance: Double
    let floorsAscended: Int
}
