package io.github.malys23.reorderiq_api.reorder;

public record ReorderResult(double dailyMean, double dailyStdDev, double z, int leadTimeDays, double safetyStock,
                            double reorderPoint, int weeksUsed, boolean insufficientHistory) {
}
