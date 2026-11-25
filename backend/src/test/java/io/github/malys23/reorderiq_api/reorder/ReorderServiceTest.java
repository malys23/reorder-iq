package io.github.malys23.reorderiq_api.reorder;

import io.github.malys23.reorderiq_api.domain.DemandHistory;
import io.github.malys23.reorderiq_api.domain.StockLevel;
import io.github.malys23.reorderiq_api.domain.Supplier;
import io.github.malys23.reorderiq_api.domain.Item;
import io.github.malys23.reorderiq_api.exception.ItemNotFoundException;
import io.github.malys23.reorderiq_api.repository.DemandHistoryRepository;
import io.github.malys23.reorderiq_api.repository.ItemRepository;
import io.github.malys23.reorderiq_api.repository.StockLevelRepository;
import io.github.malys23.reorderiq_api.repository.SupplierRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
public class ReorderServiceTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired ReorderProperties reorderProperties;
    @Autowired ReorderService reorderService;
    @Autowired SupplierRepository supplierRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired DemandHistoryRepository demandHistoryRepository;
    @Autowired StockLevelRepository stockLevelRepository;

    private static final LocalDate START = LocalDate.of(2024, 11, 4);
    private Supplier supplier;
    private Item item;

    @BeforeEach
    void setUp(){
        supplier = supplierRepository.save(new Supplier(
                1, "Test Supplier", "fasteners",
                new BigDecimal("0.15"), new BigDecimal("0.90"))
        );

        item = itemRepository.save(new Item(
            1, "Test item 1", "fasteners", supplier,
            new BigDecimal("0.35"), 9, "A"
        ));
    }

    @Test
    void reorderServiceWorksWithKnownResult(){
        demandHistoryRepository.save(new DemandHistory(
                item.getItemId(), LocalDate.parse("2024-11-04"), 60, false));
        demandHistoryRepository.save(new DemandHistory(
                item.getItemId(), LocalDate.parse("2024-11-11"), 80, false));
        demandHistoryRepository.flush();
        ReorderResult result = reorderService.calculateForItem(item.getItemId());

        assertThat(result.dailyMean()).isCloseTo(10, within(0.01));
        assertThat(result.z()).isEqualTo(2.05);
        assertThat(result.weeksUsed()).isEqualTo(2);
        assertThat(result.insufficientHistory()).isFalse();
        assertThat(result.safetyStock()).isCloseTo(32.87, within(0.01));
        assertThat(result.reorderPoint()).isCloseTo(122.87, within(0.01));
    }

    @Test
    void windowIsAlwaysTwentySix(){
        LocalDate date = START;
        for(int i=0; i<30; i++){
            if (i<4){
                demandHistoryRepository.save(new DemandHistory(
                        item.getItemId(), date.plusDays(i*7), 1000, false));
            }else{
                demandHistoryRepository.save(new DemandHistory(
                        item.getItemId(), date.plusDays(i*7), 60, false));
            }
        }
        demandHistoryRepository.flush();
        ReorderResult result = reorderService.calculateForItem(item.getItemId());

        assertThat(result.weeksUsed()).isEqualTo(26);
        assertThat(result.dailyStdDev()).isEqualTo(0.0);
        assertThat(result.dailyMean()).isCloseTo(8.571, within(0.01));
    }

    @Test
    void unknownItemThrowsError(){
        assertThatThrownBy(() ->
                reorderService.calculateForItem(999999)).isInstanceOf(ItemNotFoundException.class);
    }

    //Tests for suggested quantity service
    @Test
    void expectedLargeResultForSuggestedQuantity(){
        demandHistoryRepository.save(new DemandHistory(
                item.getItemId(), LocalDate.parse("2024-11-04"), 60, false));
        demandHistoryRepository.save(new DemandHistory(
                item.getItemId(), LocalDate.parse("2024-11-11"), 80, false));
        demandHistoryRepository.flush();

        stockLevelRepository.save(new StockLevel(
                item.getItemId(), 50, 40));
        stockLevelRepository.flush();

        int result = reorderService.suggestedQuantityForItem(item.getItemId());
        assertThat(result).isEqualTo(243);
    }

    @Test
    void expectedSmallerResultForSuggestedQuantity(){
        demandHistoryRepository.save(new DemandHistory(
                item.getItemId(), LocalDate.parse("2024-11-04"), 7, false));
        demandHistoryRepository.save(new DemandHistory(
                item.getItemId(), LocalDate.parse("2024-11-11"), 7, false));
        demandHistoryRepository.flush();

        stockLevelRepository.save(new StockLevel(
                item.getItemId(), 8, 0));
        stockLevelRepository.flush();

        int result = reorderService.suggestedQuantityForItem(item.getItemId());
        assertThat(result).isEqualTo(100);
    }

    @Test
    void noStockRowThrowsError(){
        demandHistoryRepository.save(new DemandHistory(
                item.getItemId(), LocalDate.parse("2024-11-04"), 60, false));
        demandHistoryRepository.save(new DemandHistory(
                item.getItemId(), LocalDate.parse("2024-11-11"), 80, false));
        demandHistoryRepository.flush();
        assertThatThrownBy(() ->
                reorderService.suggestedQuantityForItem(item.getItemId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No stock level");
    }
}
