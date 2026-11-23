package io.github.malys23.reorderiq_api.reorder;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReorderCalculator {
    private final ReorderProperties reorderProperties;

    public ReorderCalculator(ReorderProperties reorderProperties){
        this.reorderProperties = reorderProperties;
    }

    // Assumption: daily demand is independent from day to day, so variances add up and
    // the weekly variance is 7 times the daily one. Hence σ_daily = σ_weekly / √7.
    public record DemandStats(double dailyMean, double dailyStdDev, int weeksUsed, boolean insufficientHistory) {}
    DemandStats stats(List<Integer> weeklyQuantities){
        if(weeklyQuantities.isEmpty()){
            return new DemandStats(0, 0, 0, true);
        }

        //dailyMean calculations
        double sumMean = 0.0;
        for(Integer quantity : weeklyQuantities){
            sumMean += quantity;
        }
        double weeklyMean = sumMean/(double)weeklyQuantities.size();
        double dailyMean = weeklyMean / 7;

        if(weeklyQuantities.size() == 1){
            return new DemandStats(dailyMean, 0, 1, true);
        }

        //stdDev calculations
        double sumStdDev = 0.0;
        for(Integer quantity: weeklyQuantities){
            sumStdDev += Math.pow(quantity - weeklyMean, 2);
        }
        double stdDevWeekly = Math.sqrt(sumStdDev/(weeklyQuantities.size() - 1));
        double stdDev = stdDevWeekly / Math.sqrt(7);

        return new DemandStats(dailyMean, stdDev, weeklyQuantities.size(), false);
    }

    double safetyStock(double sigmaD, int leadTimeDays, double z){
        return z * sigmaD * Math.sqrt(leadTimeDays);
    }

    double reorderPoint(double d, int leadTimeDays, double safetyStock){
        return d * leadTimeDays + safetyStock;
    }
}
