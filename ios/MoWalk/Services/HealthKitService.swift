import Foundation
import HealthKit

final class HealthKitService: @unchecked Sendable {
    private let store = HKHealthStore()

    private let stepType = HKQuantityType(.stepCount)
    private let distanceType = HKQuantityType(.distanceWalkingRunning)

    var isAvailable: Bool { HKHealthStore.isHealthDataAvailable() }

    func requestAuthorization() async throws {
        guard isAvailable else { return }
        let types: Set = [
            stepType,
            distanceType,
            HKQuantityType(.activeEnergyBurned)
        ]
        let readTypes = Set(types)
        let shareTypes = Set([stepType, distanceType])
        try await store.requestAuthorization(toShare: shareTypes, read: readTypes)
    }

    func readSteps(from start: Date, to end: Date) async throws -> Int {
        let predicate = HKQuery.predicateForSamples(withStart: start, end: end)
        let sumOption = HKStatisticsOptions.cumulativeSum

        return try await withCheckedThrowingContinuation { continuation in
            let query = HKStatisticsQuery(
                quantityType: stepType,
                quantitySamplePredicate: predicate,
                options: sumOption
            ) { _, statistics, error in
                if let error = error {
                    continuation.resume(throwing: error)
                    return
                }
                let steps = Int(statistics?.sumQuantity()?.doubleValue(for: .count()) ?? 0)
                continuation.resume(returning: steps)
            }
            store.execute(query)
        }
    }

    func writeSteps(_ steps: Int, date: Date) async throws {
        let quantity = HKQuantity(unit: .count(), doubleValue: Double(steps))
        let sample = HKQuantitySample(
            type: stepType,
            quantity: quantity,
            start: date,
            end: date
        )
        try await store.save(sample)
    }
}
