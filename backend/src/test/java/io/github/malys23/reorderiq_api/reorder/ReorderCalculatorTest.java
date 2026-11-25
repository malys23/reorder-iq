package io.github.malys23.reorderiq_api.reorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class ReorderCalculatorTest {

    private ReorderCalculator calculator;

    @BeforeEach
    void setUp() {
        List<ReorderProperties.MinOrderTier> minOrderTiers = List.of(new ReorderProperties.MinOrderTier(new BigDecimal("10"), 100),
                new ReorderProperties.MinOrderTier(new BigDecimal("100"), 50));
        calculator = new ReorderCalculator(
                new ReorderProperties(26, Map.of("A", 2.05, "B", 1.645, "C", 1.28),
                        3, minOrderTiers, 1));
    }

    @Test
    void emptyListHasNoHistory(){
        ReorderCalculator.DemandStats stats = calculator.stats(List.of());
        assertThat(stats.insufficientHistory()).isTrue();
        assertThat(stats.weeksUsed()).isEqualTo(0);
        assertThat(stats.dailyStdDev()).isEqualTo(0.0);
        assertThat(stats.dailyMean()).isEqualTo(0.0);
    }

    @Test
    void oneWeekIsInsufficient(){
        ReorderCalculator.DemandStats stats = calculator.stats(List.of(70));
        assertThat(stats.insufficientHistory()).isTrue();
        assertThat(stats.weeksUsed()).isEqualTo(1);
        assertThat(stats.dailyStdDev()).isEqualTo(0.0);
        assertThat(stats.dailyMean()).isCloseTo(10, within(0.01));
    }

    @Test
    void twoWeeksIsSufficient(){
        ReorderCalculator.DemandStats stats = calculator.stats(List.of(60,80));
        assertThat(stats.insufficientHistory()).isFalse();
        assertThat(stats.weeksUsed()).isEqualTo(2);
        assertThat(stats.dailyStdDev()).isCloseTo(5.345, within(0.01));
        assertThat(stats.dailyMean()).isCloseTo(10, within(0.01));
    }

    @Test
    void manyWeeksIsSufficient(){
        ReorderCalculator.DemandStats stats = calculator.stats(Collections.nCopies(26,70));
        assertThat(stats.insufficientHistory()).isFalse();
        assertThat(stats.weeksUsed()).isEqualTo(26);
        assertThat(stats.dailyStdDev()).isEqualTo(0.0);
        assertThat(stats.dailyMean()).isCloseTo(10, within(0.01));
    }

    @Test
    void handExample(){
        double ss = calculator.safetyStock(3, 9, 1.645);
        double rop = calculator.reorderPoint(10, 9, ss);
        assertThat(ss).isCloseTo(14.805, within(0.01));
        assertThat(rop).isCloseTo(104.805, within(0.01));
    }

    @Test
    void zeroVariationExample(){
        double ss = calculator.safetyStock(0, 9, 1.645);
        double rop = calculator.reorderPoint(10, 9, ss);
        assertThat(ss).isEqualTo(0.0);
        assertThat(rop).isEqualTo(90.0);
    }

    //Tests for getting min order quantity
    @Test
    void belowTenMatchesResult(){
        int result = calculator.minOrderQuantity(new BigDecimal("9.99"));
        assertThat(result).isEqualTo(100);
    }

    @Test
    void tenMatchesResult(){
        int result = calculator.minOrderQuantity(new BigDecimal("10.00"));
        assertThat(result).isEqualTo(50);
    }

    @Test
    void lessThanHundredMatchesResult(){
        int result = calculator.minOrderQuantity(new BigDecimal("99.99"));
        assertThat(result).isEqualTo(50);
    }

    @Test
    void hundredMatchesResult(){
        int result = calculator.minOrderQuantity(new BigDecimal("100.00"));
        assertThat(result).isEqualTo(1);
    }

    //Tests for suggested order quantity
    @Test
    void noOrderExactlyAtROP(){
        int result = calculator.suggestedQuantity(100, 10, 50, 50, 50);
        assertThat(result).isEqualTo(0);
    }

    @Test
    void noOrderWellAboveROP(){
        int result = calculator.suggestedQuantity(100, 10, 100, 150, 50);
        assertThat(result).isEqualTo(0);
    }

    @Test
    void suggestedQuantityNormalOrder(){
        int result = calculator.suggestedQuantity(100, 10, 40, 20, 50);
        assertThat(result).isEqualTo(250);
    }

    @Test
    void suggestedQuantityMinimumApplies(){
        int result = calculator.suggestedQuantity(100, 10, 50, 40, 500);
        assertThat(result).isEqualTo(500);
    }

    @Test
    void suggestedQuantityRoundsUp(){
        int result = calculator.suggestedQuantity(104.805, 10, 20, 40, 50);
        assertThat(result).isEqualTo(255);
    }

    @Test
    void boundaryJustBelowROP(){
        int result = calculator.suggestedQuantity(100, 10, 40, 59, 50);
        assertThat(result).isEqualTo(211);
    }

    //Tests for calculator
    @Test
    void calculateZeroVariation(){
        ReorderResult result = calculator.calculate(Collections.nCopies(26, 70), 9, "A");
        assertThat(result.reorderPoint()).isCloseTo(90, within(0.01));
        assertThat(result.safetyStock()).isEqualTo(0.0);
        assertThat(result.dailyStdDev()).isEqualTo(0.0);
        assertThat(result.dailyMean()).isCloseTo(10, within(0.01));
    }

    @Test
    void calculateWithRealListClassA(){
        ReorderResult result = calculator.calculate(List.of(60,80), 9, "A");
        assertThat(result.reorderPoint()).isCloseTo(122.87, within(0.01));
        assertThat(result.safetyStock()).isCloseTo(32.87, within(0.01));
        assertThat(result.z()).isEqualTo(2.05);
        assertThat(result.weeksUsed()).isEqualTo(2);
        assertThat(result.insufficientHistory()).isFalse();
    }

    @Test
    void calculateWithRealListClassB(){
        ReorderResult result = calculator.calculate(List.of(60,80), 9, "B");
        assertThat(result.reorderPoint()).isCloseTo(116.38, within(0.01));
        assertThat(result.safetyStock()).isCloseTo(26.38, within(0.01));
        assertThat(result.z()).isEqualTo(1.645);
        assertThat(result.weeksUsed()).isEqualTo(2);
        assertThat(result.insufficientHistory()).isFalse();
    }
    @Test
    void calculateWithRealListClassC(){
        ReorderResult result = calculator.calculate(List.of(60,80), 9, "C");
        assertThat(result.reorderPoint()).isCloseTo(110.53, within(0.01));
        assertThat(result.safetyStock()).isCloseTo(20.53, within(0.01));
        assertThat(result.z()).isEqualTo(1.28);
        assertThat(result.weeksUsed()).isEqualTo(2);
        assertThat(result.insufficientHistory()).isFalse();
    }

    @Test
    void calculateErrors(){
        assertThatThrownBy(()->calculator.calculate(List.of(60,80), 9, "D"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("D");
    }

    //Testing classification
    @Test
    void classifyStockOutStatus(){
        StockStatus result = calculator.classify(0, 20, 50.0, 10.0);
        assertThat(result).isEqualTo(StockStatus.STOCKOUT);
    }

    @Test
    void classifyBelowROPStatus(){
        StockStatus result = calculator.classify(20, 20, 50.0, 10.0);
        assertThat(result).isEqualTo(StockStatus.BELOW_REORDER_POINT);
    }

    @Test
    void classifyExcessStatus(){
        StockStatus result = calculator.classify(900, 100, 50.0, 10.0);
        assertThat(result).isEqualTo(StockStatus.EXCESS);
    }

    @Test
    void classifyOkStatus(){
        StockStatus result = calculator.classify(45, 10, 50.0, 10.0);
        assertThat(result).isEqualTo(StockStatus.OK);
    }

    @Test
    void classifyExactlyAtROPStatus(){
        StockStatus result = calculator.classify(40, 10, 50.0, 10.0);
        assertThat(result).isEqualTo(StockStatus.OK);
    }

    @Test
    void classifyExactlyAtExcessStatus(){
        StockStatus result = calculator.classify(900, 50, 50.0, 10.0);
        assertThat(result).isEqualTo(StockStatus.OK);
    }

    @Test
    void classifyConsidersOnOrder(){
        StockStatus result = calculator.classify(10, 100, 50, 10);
        assertThat(result).isEqualTo(StockStatus.OK);
    }

    @Test
    void classifyNoHistoryCaseAsExcess(){
        StockStatus result = calculator.classify(5, 0, 0, 0);
        assertThat(result).isEqualTo(StockStatus.EXCESS);
    }
}
