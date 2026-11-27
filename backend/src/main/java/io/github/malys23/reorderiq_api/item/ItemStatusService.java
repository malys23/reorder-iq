package io.github.malys23.reorderiq_api.item;

import io.github.malys23.reorderiq_api.domain.DemandHistory;
import io.github.malys23.reorderiq_api.domain.Item;
import io.github.malys23.reorderiq_api.domain.StockLevel;
import io.github.malys23.reorderiq_api.reorder.*;
import io.github.malys23.reorderiq_api.repository.DemandHistoryRepository;
import io.github.malys23.reorderiq_api.repository.ItemRepository;
import io.github.malys23.reorderiq_api.repository.StockLevelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Transactional(readOnly = true)
@Service
public class ItemStatusService {
    private final ItemRepository itemRepository;
    private final StockLevelRepository stockLevelRepository;
    private final DemandHistoryRepository demandHistoryRepository;
    private final ReorderCalculator reorderCalculator;
    private final ReorderProperties reorderProperties;

    public ItemStatusService(ItemRepository itemRepository, StockLevelRepository stockLevelRepository, DemandHistoryRepository demandHistoryRepository,
                          ReorderCalculator reorderCalculator, ReorderProperties reorderProperties){
        this.itemRepository = itemRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.demandHistoryRepository = demandHistoryRepository;
        this.reorderCalculator = reorderCalculator;
        this.reorderProperties = reorderProperties;
    }

    public List<ItemView> loadAll(){
        List<ItemView> views = new ArrayList<>();
        Map<Integer, List<Integer>> demandByItem = new HashMap<>();

        Optional<LocalDate> latest = demandHistoryRepository.findLatestWeek();
        if (latest.isPresent()){
            LocalDate cutoff = latest.get().minusWeeks(reorderProperties.windowWeeks()-1);
            for (DemandHistory row : demandHistoryRepository.findByWeekStartGreaterThanEqual(cutoff)){
                demandByItem.computeIfAbsent(row.getItemId(), id -> new ArrayList<>()).add(row.getQuantity());
            }
        }

        Map<Integer, StockLevel> stockByItem = new HashMap<>();
        for(StockLevel s: stockLevelRepository.findAll()){
            stockByItem.put(s.getItemId(), s);
        }

        for(Item item : itemRepository.findAll()){
            StockLevel stockLevel = stockByItem.get(item.getItemId());
            if (stockLevel == null){
                throw new IllegalStateException("No stock level for item " + item.getItemId());
            }
            List<Integer> weeklyQuantities = demandByItem.getOrDefault(item.getItemId(), List.of());
            ReorderResult reorderResult = reorderCalculator.calculate(weeklyQuantities, item.getLeadTimeDays(), item.getAbcClass());
            StockStatus stockStatus = reorderCalculator.classify(stockLevel.getOnHand(),
                    stockLevel.getOnOrder(), reorderResult.reorderPoint(), reorderResult.dailyMean());
            views.add(new ItemView(item, stockLevel, reorderResult, stockStatus));
        }

        return views;
    }
}
