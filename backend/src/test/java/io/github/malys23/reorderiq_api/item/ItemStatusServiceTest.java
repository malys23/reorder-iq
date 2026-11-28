package io.github.malys23.reorderiq_api.item;

import io.github.malys23.reorderiq_api.domain.DemandHistory;
import io.github.malys23.reorderiq_api.domain.Item;
import io.github.malys23.reorderiq_api.domain.StockLevel;
import io.github.malys23.reorderiq_api.domain.Supplier;
import io.github.malys23.reorderiq_api.reorder.ReorderResult;
import io.github.malys23.reorderiq_api.reorder.ReorderService;
import io.github.malys23.reorderiq_api.reorder.StockStatus;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
public class ItemStatusServiceTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired SupplierRepository supplierRepository;
    @Autowired ItemStatusService itemStatusService;
    @Autowired ItemRepository itemRepository;
    @Autowired StockLevelRepository stockLevelRepository;
    @Autowired DemandHistoryRepository demandHistoryRepository;
    @Autowired ReorderService reorderService;

    private static final LocalDate START = LocalDate.of(2024, 11, 4);
    private Supplier supplier;

    private void saveItemAndStock(int id, int onHand, int onOrder, BigDecimal cost){
        itemRepository.save(new Item(
                id, "Test item " + id, "fasteners", supplier, cost, 9, "A"));
        stockLevelRepository.save(new StockLevel(id ,onHand, onOrder));
    }

    private void saveFlatDemand(int itemId, int weeks, int quantity){
        for(int i=0; i<weeks; i++){
            demandHistoryRepository.save(new DemandHistory(
                    itemId, START.plusWeeks(i), quantity, false));
        }
    }

    private Map<Integer, StockStatus> statusesById(List<ItemView> result){
        Map<Integer, StockStatus> statuses = new HashMap<>();
        for(ItemView itemView : result){
            statuses.put(itemView.item().getItemId(), itemView.stockStatus());
        }
        return statuses;
    }

    private Map<Integer, ReorderResult> resultsById(List<ItemView> result){
        Map<Integer, ReorderResult> reorderResults = new HashMap<>();
        for(ItemView itemView : result){
            reorderResults.put(itemView.item().getItemId(), itemView.reorderResult());
        }
        return reorderResults;
    }

    @BeforeEach
    void setUp(){
        supplier = supplierRepository.save(new Supplier(
                1, "Test Supplier", "fasteners",
                new BigDecimal("0.15"), new BigDecimal("0.90")));

        saveItemAndStock(1, 0, 50, new BigDecimal("0.35"));
        saveItemAndStock(2, 40, 20, new BigDecimal("0.35"));
        saveItemAndStock(3, 100, 0, new BigDecimal("0.35"));
        saveItemAndStock(4, 1000, 100, new BigDecimal("2.00"));

        for(Item item: itemRepository.findAll()){
            saveFlatDemand(item.getItemId(), 4, 70);
        }

        supplierRepository.flush();
        itemRepository.flush();
        stockLevelRepository.flush();
        demandHistoryRepository.flush();
    }

    @Test
    void itemReturnsCorrectSizeAndStatus(){
        List<ItemView> result = itemStatusService.loadAll();
        Map<Integer, StockStatus> statuses = statusesById(result);

        assertThat(result).hasSize(4);
        assertThat(statuses.get(1)).isEqualTo(StockStatus.STOCKOUT);
        assertThat(statuses.get(2)).isEqualTo(StockStatus.BELOW_REORDER_POINT);
        assertThat(statuses.get(3)).isEqualTo(StockStatus.OK);
        assertThat(statuses.get(4)).isEqualTo(StockStatus.EXCESS);
    }

    @Test
    void reorderPointsAreNinety(){
        List<ItemView> result = itemStatusService.loadAll();
        Map<Integer, ReorderResult> reorderResults = resultsById(result);

        assertThat(reorderResults.get(1).reorderPoint()).isCloseTo(90, within(0.001));
        assertThat(reorderResults.get(2).reorderPoint()).isCloseTo(90, within(0.001));
        assertThat(reorderResults.get(3).reorderPoint()).isCloseTo(90, within(0.001));
        assertThat(reorderResults.get(4).reorderPoint()).isCloseTo(90, within(0.001));
    }

    @Test
    void loadAllAndPerItemAgree(){
        List<ItemView> result = itemStatusService.loadAll();
        Map<Integer, ReorderResult> reorderResults = resultsById(result);

        assertThat(reorderResults.get(1).reorderPoint()).isEqualTo(reorderService.calculateForItem(1).reorderPoint());
        assertThat(reorderResults.get(2).reorderPoint()).isEqualTo(reorderService.calculateForItem(2).reorderPoint());
        assertThat(reorderResults.get(3).reorderPoint()).isEqualTo(reorderService.calculateForItem(3).reorderPoint());
        assertThat(reorderResults.get(4).reorderPoint()).isEqualTo(reorderService.calculateForItem(4).reorderPoint());
    }

    @Test
    void noDemandNoOnHandCaseGivesInsufficientHistoryAndStockOut(){
        saveItemAndStock(5, 0, 50, new BigDecimal("0.35"));
        List<ItemView> result = itemStatusService.loadAll();

        Map<Integer, StockStatus> statuses = statusesById(result);
        Map<Integer, ReorderResult> reorderResults = resultsById(result);

        assertThat(statuses.get(5)).isEqualTo(StockStatus.STOCKOUT);
        assertThat(reorderResults.get(5).insufficientHistory()).isTrue();
    }

    @Test
    void noDemandCaseGivesInsufficientHistoryAndExcess(){
        saveItemAndStock(6, 40, 50, new BigDecimal("0.35"));
        List<ItemView> result = itemStatusService.loadAll();

        Map<Integer, StockStatus> statuses = statusesById(result);
        Map<Integer, ReorderResult> reorderResults = resultsById(result);

        assertThat(statuses.get(6)).isEqualTo(StockStatus.EXCESS);
        assertThat(reorderResults.get(6).insufficientHistory()).isTrue();
    }

    @Test
    void missingStockRowThrowsError(){
        itemRepository.save(new Item(
                7, "Test item " + 7, "fasteners", supplier, new BigDecimal("0.35"), 9, "A"));

        assertThatThrownBy(()-> itemStatusService.loadAll())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("7");
    }
}
