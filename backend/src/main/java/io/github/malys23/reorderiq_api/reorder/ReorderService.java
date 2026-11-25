package io.github.malys23.reorderiq_api.reorder;

import io.github.malys23.reorderiq_api.domain.StockLevel;
import io.github.malys23.reorderiq_api.exception.ItemNotFoundException;
import io.github.malys23.reorderiq_api.domain.DemandHistory;
import io.github.malys23.reorderiq_api.domain.Item;
import io.github.malys23.reorderiq_api.repository.DemandHistoryRepository;
import io.github.malys23.reorderiq_api.repository.ItemRepository;
import io.github.malys23.reorderiq_api.repository.StockLevelRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReorderService {
    private final ItemRepository itemRepository;
    private final DemandHistoryRepository demandHistoryRepository;
    private final StockLevelRepository stockLevelRepository;
    private final ReorderCalculator reorderCalculator;
    private final ReorderProperties reorderProperties;

    public ReorderService(ItemRepository itemRepository, DemandHistoryRepository demandHistoryRepository,
                          StockLevelRepository stockLevelRepository, ReorderCalculator reorderCalculator,
                          ReorderProperties reorderProperties){
        this.itemRepository = itemRepository;
        this.demandHistoryRepository = demandHistoryRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.reorderCalculator = reorderCalculator;
        this.reorderProperties = reorderProperties;
    }

    public ReorderResult calculateForItem(Integer itemId){
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));

        List<DemandHistory> history = demandHistoryRepository.findByItemIdOrderByWeekStartDesc(
                itemId, PageRequest.of(0, reorderProperties.windowWeeks()));

        List<Integer> quantities = new ArrayList<>();
        for (DemandHistory row : history){
            quantities.add(row.getQuantity());
        }

        return reorderCalculator.calculate(quantities, item.getLeadTimeDays(), item.getAbcClass());
    }

    public int suggestedQuantityForItem(Integer itemId){
        ReorderResult result = calculateForItem(itemId);
        StockLevel stock = stockLevelRepository.findById(itemId).orElseThrow(() -> new IllegalStateException("No stock level for item " + itemId));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        int minOrderQuantity = reorderCalculator.minOrderQuantity(item.getUnitCost());

        return reorderCalculator.suggestedQuantity(result.reorderPoint(), result.dailyMean(), stock.getOnHand(), stock.getOnOrder(), minOrderQuantity);
    }

}
