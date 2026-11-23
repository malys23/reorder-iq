package io.github.malys23.reorderiq_api.reorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class ReorderCalculatorTest {

    private ReorderCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ReorderCalculator(
                new ReorderProperties(26, Map.of("A", 2.05, "B", 1.645, "C", 1.28)));
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
        assertThat(stats.dailyMean()).isCloseTo(10, within(0.001));
    }

    @Test
    void twoWeeksIsSufficient(){
        ReorderCalculator.DemandStats stats = calculator.stats(List.of(60,80));
        assertThat(stats.insufficientHistory()).isFalse();
        assertThat(stats.weeksUsed()).isEqualTo(2);
        assertThat(stats.dailyStdDev()).isCloseTo(5.345, within(0.001));
        assertThat(stats.dailyMean()).isCloseTo(10, within(0.001));
    }

    @Test
    void manyWeeksIsSufficient(){
        ReorderCalculator.DemandStats stats = calculator.stats(Collections.nCopies(26,70));
        assertThat(stats.insufficientHistory()).isFalse();
        assertThat(stats.weeksUsed()).isEqualTo(26);
        assertThat(stats.dailyStdDev()).isEqualTo(0.0);
        assertThat(stats.dailyMean()).isCloseTo(10, within(0.001));
    }

    @Test
    void workedExample(){
        double ss = calculator.safetyStock(3, 9, 1.645);
        double rop = calculator.reorderPoint(10, 9, ss);
        assertThat(ss).isCloseTo(14.805, within(0.001));
        assertThat(rop).isCloseTo(104.805, within(0.001));
    }

    @Test
    void zeroVariationExample(){
        double ss = calculator.safetyStock(0, 9, 1.645);
        double rop = calculator.reorderPoint(10, 9, ss);
        assertThat(ss).isEqualTo(0.0);
        assertThat(rop).isEqualTo(90.0);
    }
}
