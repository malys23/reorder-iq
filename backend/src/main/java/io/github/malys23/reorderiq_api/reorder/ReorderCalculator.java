package io.github.malys23.reorderiq_api.reorder;

import io.github.malys23.reorderiq_api.domain.DemandHistory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ReorderCalculator {
    private final ReorderProperties reorderProperties;

    public ReorderCalculator(ReorderProperties reorderProperties){
        this.reorderProperties = reorderProperties;
    }

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
        // Assumption: daily demand is independent from day to day, so variances add up and
        // the weekly variance is 7 times the daily one. Hence σ_daily = σ_weekly / √7.
        double dailyMean = weeklyMean / 7;

        if(weeklyQuantities.size() == 1){
            return new DemandStats(dailyMean, 0, 1, true);
        }

        //stdDev calculations
        double sumStdDev = 0.0;
        for(Integer quantity: weeklyQuantities){
            sumStdDev += Math.pow(quantity - weeklyMean, 2);
        }
        double weeklyStdDev = Math.sqrt(sumStdDev/(weeklyQuantities.size() - 1));
        double dailyStdDev = weeklyStdDev / Math.sqrt(7);

        return new DemandStats(dailyMean, dailyStdDev, weeklyQuantities.size(), false);
    }

    double safetyStock(double sigmaD, int leadTimeDays, double z){
        return z * sigmaD * Math.sqrt(leadTimeDays);
    }

    double reorderPoint(double d, int leadTimeDays, double safetyStock){
        return d * leadTimeDays + safetyStock;
    }

    public ReorderResult calculate(List<Integer> weeklyQuantities, int leadTimeDays, String abcClass){
        DemandStats result = stats(weeklyQuantities);

        Double z = reorderProperties.zValues().get(abcClass);
        if(z== null) throw new IllegalArgumentException("Unknown ABC class: " + abcClass + ". Valid classes: "
                + reorderProperties.zValues().keySet());

        double safetyStock = safetyStock(result.dailyStdDev, leadTimeDays, z);
        double reorderPoint = reorderPoint(result.dailyMean, leadTimeDays, safetyStock);
        return new ReorderResult(result.dailyMean(), result.dailyStdDev(), z, leadTimeDays, safetyStock, reorderPoint,
                result.weeksUsed(), result.insufficientHistory());
    }

    //get min order quantity
    int minOrderQuantity(BigDecimal unitCost){
        for (ReorderProperties.MinOrderTier tier: reorderProperties.minOrderTiers()){
            if(unitCost.compareTo(tier.belowUnitCost())< 0){
                return tier.quantity();
            }
        }
        return reorderProperties.defaultMinOrderQuantity();
    }

    //calculate suggested value
    int suggestedQuantity(double reorderPoint, double dailyMean, int onHand, int onOrder, int minOrderQuantity){
        int position = onHand + onOrder;
        if(position >= reorderPoint){ return 0; }
        double orderUpTo = reorderPoint + dailyMean * 7 * reorderProperties.orderCoverWeeks();
        int raw = (int)Math.ceil(orderUpTo - position - 1e-9);  //weekly mean / 7, multiplied back, can land just above whole number, and ceil would add a unit
        return Math.max(raw, minOrderQuantity);
    }
}