package com.bookcatalog.bookcatalog.service.strategy;

import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteUserByEmailStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteUserByIdStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteUserByUsernameStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateUserByEmailStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateUserByIdStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateUserByUsernameStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserStrategyFactory {

    private final Map<String, UpdateStrategy<User>> updateStrategies;
    private final Map<String, DeleteStrategy<User>> deleteStrategies;

    @Autowired
    public UserStrategyFactory(List<UpdateStrategy<User>> updateStrategiesList,
                               List<DeleteStrategy<User>> deleteStrategiesList) {
        this.updateStrategies = new HashMap<>();
        this.deleteStrategies = new HashMap<>();
        registerUpdateStrategies(updateStrategiesList);
        registerDeleteStrategies(deleteStrategiesList);
    }

    private void registerUpdateStrategies(List<UpdateStrategy<User>> strategies) {
        for (UpdateStrategy<User> strategy : strategies) {
            if (strategy instanceof UpdateUserByIdStrategy) {
                updateStrategies.put("id", strategy);
            } else if (strategy instanceof UpdateUserByUsernameStrategy) {
                updateStrategies.put("username", strategy);
            } else if (strategy instanceof UpdateUserByEmailStrategy) {
                updateStrategies.put("email", strategy);
            }
        }
    }

    private void registerDeleteStrategies(List<DeleteStrategy<User>> strategies) {
        for (DeleteStrategy<User> strategy : strategies) {
            if (strategy instanceof DeleteUserByIdStrategy) {
                deleteStrategies.put("id", strategy);
            } else if (strategy instanceof DeleteUserByUsernameStrategy) {
                deleteStrategies.put("username", strategy);
            } else if (strategy instanceof DeleteUserByEmailStrategy) {
                deleteStrategies.put("email", strategy);
            }
        }
    }

    public UpdateStrategy<User> getUpdateStrategy(String type) {
        return updateStrategies.get(type);
    }

    public DeleteStrategy<User> getDeleteStrategy(String type) {
        return deleteStrategies.get(type);
    }
}
