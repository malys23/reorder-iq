package io.github.malys23.reorderiq_api.item;

import io.github.malys23.reorderiq_api.domain.Item;
import io.github.malys23.reorderiq_api.domain.StockLevel;
import io.github.malys23.reorderiq_api.domain.Supplier;
import io.github.malys23.reorderiq_api.reorder.ReorderCalculator;
import io.github.malys23.reorderiq_api.reorder.ReorderProperties;
import io.github.malys23.reorderiq_api.repository.DemandHistoryRepository;
import io.github.malys23.reorderiq_api.repository.ItemRepository;
import io.github.malys23.reorderiq_api.repository.StockLevelRepository;
import io.github.malys23.reorderiq_api.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;

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
    @Autowired ReorderCalculator reorderCalculator;
    @Autowired ReorderProperties reorderProperties;

    private Supplier supplier;
    private Item item;
    private StockLevel stockLevel;

    @BeforeEach
    void setUp(){
        supplier = supplierRepository.save(new Supplier(
                1, "Test Supplier", "fasteners",
                new BigDecimal("0.15"), new BigDecimal("0.90"))
        );

    }
}
