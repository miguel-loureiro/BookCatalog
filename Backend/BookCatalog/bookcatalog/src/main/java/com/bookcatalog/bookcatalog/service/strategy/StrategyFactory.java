package com.bookcatalog.bookcatalog.service.strategy;

import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteBookByISBNStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteBookByIdStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteBookByTitleStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateBookByISBNStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateBookByIdStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateBookByTitleStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StrategyFactory<T> {

    private final Map<String, UpdateStrategy<T>> updateStrategies;
    private final Map<String, DeleteStrategy<T>> deleteStrategies;

    @Autowired
    public StrategyFactory(List<UpdateStrategy<T>> updateStrategiesList,
                           List<DeleteStrategy<T>> deleteStrategiesList) {
        this.updateStrategies = new HashMap<>();
        this.deleteStrategies = new HashMap<>();
        registerUpdateStrategies(updateStrategiesList);
        registerDeleteStrategies(deleteStrategiesList);
    }

    private void registerUpdateStrategies(List<UpdateStrategy<T>> strategies) {
        for (UpdateStrategy<T> strategy : strategies) {
            if (strategy instanceof UpdateBookByIdStrategy) {
                updateStrategies.put("id", strategy);
            } else if (strategy instanceof UpdateBookByTitleStrategy) {
                updateStrategies.put("title", strategy);
            } else if (strategy instanceof UpdateBookByISBNStrategy) {
                updateStrategies.put("isbn", strategy);
            }
        }
    }

    private void registerDeleteStrategies(List<DeleteStrategy<T>> strategies) {
        for (DeleteStrategy<T> strategy : strategies) {
            if (strategy instanceof DeleteBookByIdStrategy) {
                deleteStrategies.put("id", strategy);
            } else if (strategy instanceof DeleteBookByTitleStrategy) {
                deleteStrategies.put("title", strategy);
            } else if (strategy instanceof DeleteBookByISBNStrategy) {
                deleteStrategies.put("isbn", strategy);
            }
        }
    }

    public UpdateStrategy<T> getUpdateStrategy(String type) {
        return updateStrategies.get(type);
    }

    public DeleteStrategy<T> getDeleteStrategy(String type) {
        return deleteStrategies.get(type);
    }
}
