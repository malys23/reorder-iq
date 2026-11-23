package io.github.malys23.reorderiq_api.exception;

public class ItemNotFoundException extends RuntimeException{
    public ItemNotFoundException(Integer itemId){
        super("Item not found: " + itemId);
    }
}
