package io.github.malys23.reorderiq_api.repository;

import io.github.malys23.reorderiq_api.domain.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ItemAndDemandRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired SupplierRepository supplierRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired StockLevelRepository stockLevelRepository;
    @Autowired DemandHistoryRepository demandHistoryRepository;
    @Autowired EntityManager entityManager;

    private static final LocalDate START = LocalDate.of(2024, 11, 4);
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = supplierRepository.save(new Supplier(
                1, "Test Supplier", "fasteners",
                new BigDecimal("0.15"), new BigDecimal("0.90"))
        );
    }

    private Item saveItem(int id){
        return itemRepository.save(new Item(
                id, "Test item " + id, "fasteners", supplier,
                new BigDecimal("0.35"), 14, "A"
        ));
    }

    private void saveWeeks(int itemId, int weeks){
        for (int w=0; w<weeks; w++){
            demandHistoryRepository.save(new DemandHistory(
                    itemId, START.plusWeeks(w), 10+w, false
            ));
        }
    }

    private void flushAndClear(){
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void savesAndReadsBackItemWithSupplier(){
        saveItem(1);
        flushAndClear();

        Item found = itemRepository.findById(1).orElseThrow();

        assertThat(found.getName()).isEqualTo("Test item 1");
        assertThat(found.getCategory()).isEqualTo("fasteners");
        assertThat(found.getUnitCost()).isEqualTo("0.35");
        assertThat(found.getLeadTimeDays()).isEqualTo(14);
        assertThat(found.getAbcClass()).isEqualTo("A");
        assertThat(found.getSupplier().getSupplierId()).isEqualTo(1);
    }

    @Test
    void savesAndReadsBackStockLevel(){
        saveItem(1);
        stockLevelRepository.save(new StockLevel(1, 120, 30));
        flushAndClear();

        StockLevel found = stockLevelRepository.findById(1).orElseThrow();

        assertThat(found.getOnHand()).isEqualTo(120);
        assertThat(found.getOnOrder()).isEqualTo(30);
    }

    @Test
    void differentWeeksForSameItemAreSeparateRows(){
        saveItem(1);
        saveWeeks(1,2);
        flushAndClear();

        assertThat(demandHistoryRepository.count()).isEqualTo(2);
        assertThat(demandHistoryRepository.findById(new DemandHistoryId(1, START.plusWeeks(1))))
                .isPresent();
    }

    @Test
    void returnsLastNWeeksNewestFirstForOneItemOnly(){
        saveItem(1);
        saveItem(2);
        saveWeeks(1, 30);
        saveWeeks(2, 5);
        flushAndClear();

        List<DemandHistory> last26 = demandHistoryRepository
                .findByItemIdOrderByWeekStartDesc(1, PageRequest.of(0,26));

        assertThat(last26).hasSize(26);
        assertThat(last26).allMatch(row -> row.getItemId()==1);
        assertThat(last26.get(0).getWeekStart()).isEqualTo(START.plusWeeks(29));
        assertThat(last26.get(25).getWeekStart()).isEqualTo(START.plusWeeks(4));
    }
}
