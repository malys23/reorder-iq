package io.github.malys23.reorderiq_api.item;

import io.github.malys23.reorderiq_api.domain.Item;
import io.github.malys23.reorderiq_api.domain.StockLevel;
import io.github.malys23.reorderiq_api.reorder.ReorderResult;
import io.github.malys23.reorderiq_api.reorder.StockStatus;

public record ItemView(Item item, StockLevel stockLevel, ReorderResult reorderResult, StockStatus stockStatus) {
}
