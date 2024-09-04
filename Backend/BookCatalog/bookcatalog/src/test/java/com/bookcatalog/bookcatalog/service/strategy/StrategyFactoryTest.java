package com.bookcatalog.bookcatalog.service.strategy;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteBookByISBNStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteBookByIdStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteBookByTitleStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateBookByISBNStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateBookByIdStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateBookByTitleStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StrategyFactoryTest {

    private StrategyFactory<Book> strategyFactory;

    @Mock
    private UpdateBookByIdStrategy updateBookByIdStrategy;
    @Mock
    private UpdateBookByTitleStrategy updateBookByTitleStrategy;
    @Mock
    private UpdateBookByISBNStrategy updateBookByISBNStrategy;

    @Mock
    private DeleteBookByIdStrategy deleteBookByIdStrategy;
    @Mock
    private DeleteBookByTitleStrategy deleteBookByTitleStrategy;
    @Mock
    private DeleteBookByISBNStrategy deleteBookByISBNStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        List<UpdateStrategy<Book>> updateStrategiesList = List.of(updateBookByIdStrategy, updateBookByTitleStrategy, updateBookByISBNStrategy);
        List<DeleteStrategy<Book>> deleteStrategiesList = List.of(deleteBookByIdStrategy, deleteBookByTitleStrategy, deleteBookByISBNStrategy);

        strategyFactory = new StrategyFactory<>(updateStrategiesList, deleteStrategiesList);
    }

    @Test
    void testGetUpdateStrategyById() {
        UpdateStrategy<Book> strategy = strategyFactory.getUpdateStrategy("id");
        assertNotNull(strategy);
        assertEquals(updateBookByIdStrategy, strategy);
    }

    @Test
    void testGetUpdateStrategyByTitle() {
        UpdateStrategy<Book> strategy = strategyFactory.getUpdateStrategy("title");
        assertNotNull(strategy);
        assertEquals(updateBookByTitleStrategy, strategy);
    }

    @Test
    void testGetUpdateStrategyByISBN() {
        UpdateStrategy<Book> strategy = strategyFactory.getUpdateStrategy("isbn");
        assertNotNull(strategy);
        assertEquals(updateBookByISBNStrategy, strategy);
    }

    @Test
    void testGetDeleteStrategyById() {
        DeleteStrategy<Book> strategy = strategyFactory.getDeleteStrategy("id");
        assertNotNull(strategy);
        assertEquals(deleteBookByIdStrategy, strategy);
    }

    @Test
    void testGetDeleteStrategyByTitle() {
        DeleteStrategy<Book> strategy = strategyFactory.getDeleteStrategy("title");
        assertNotNull(strategy);
        assertEquals(deleteBookByTitleStrategy, strategy);
    }

    @Test
    void testGetDeleteStrategyByISBN() {
        DeleteStrategy<Book> strategy = strategyFactory.getDeleteStrategy("isbn");
        assertNotNull(strategy);
        assertEquals(deleteBookByISBNStrategy, strategy);
    }

    @Test
    void testGetUpdateStrategyForNonExistentType() {
        UpdateStrategy<Book> strategy = strategyFactory.getUpdateStrategy("nonExistentType");
        assertNull(strategy);
    }

    @Test
    void testGetDeleteStrategyForNonExistentType() {
        DeleteStrategy<Book> strategy = strategyFactory.getDeleteStrategy("nonExistentType");
        assertNull(strategy);
    }
}