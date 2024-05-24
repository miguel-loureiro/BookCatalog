package com.bookcatalog.bookcatalog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    private Book book;

    @BeforeEach
    public void setUp() throws IOException {

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateBook() {

        //act
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book createdBook = bookService.createBook(book);

        //assert
        assertEquals(book, createdBook);
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void testGetBookById_IsFound() throws IOException {
        
        //arrange
        Book book = new Book();
 
        //act
        when(bookRepository.findById(anyInt())).thenReturn(Optional.of(book));

        Optional<Book> foundBook = bookService.getBookById(5);

        //assert
        assertTrue(foundBook.isPresent());
        assertEquals(book, foundBook.get());
        verify(bookRepository, times(1)).findById(5);
    }

    @Test
    public void testGetBookById_NotFound() {

        when(bookRepository.findById(anyInt())).thenReturn(Optional.empty());

        Optional<Book> foundBook = bookService.getBookById(1);

        assertFalse(foundBook.isPresent());
        verify(bookRepository, times(1)).findById(1);
    }

    @Test
    void testGetAllBooks() throws IOException {
 
        //arrange
        Book book1 = new Book();
        book1.setId(1);
        book1.setTitle("Test Title1");
        book1.setAuthor("Test Author1");
        book1.setPrice("1.00");
        book1.setPublishDate("01/1999");
        book1.setCoverImage("testfile1");

        Book book2 = new Book();
        book2.setId(2);
        book2.setTitle("Test Title2");
        book2.setAuthor("Test Author2");
        book2.setPrice("2.00");
        book2.setPublishDate("02/2000");
        book2.setCoverImage("testfile2");

        List<Book> books = Arrays.asList(book1, book2);
        
        when(bookService.getAllBooks()).thenReturn(books);

        //act
        List<Book> allBooks = bookService.getAllBooks();

        //assert
        assertEquals(2, allBooks.size());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void testUpdateBook_Success_WithFile() throws Exception {

        //arrange
        book = new Book();
        book.setId(1);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setPrice("100.0");
        book.setPublishDate("01/1999");

        Book newDetails = new Book();
        newDetails.setTitle("Updated Title");
        newDetails.setAuthor("Updated Author");
        newDetails.setPrice("200.0");
        newDetails.setPublishDate("12/2003");

        MockMultipartFile file = new MockMultipartFile("file", "new_image.jpg", "image/jpeg", new byte[] {1, 2, 3});

        when(bookRepository.findById(anyInt())).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        //act
        Book updatedBook = bookService.updateBook(1, newDetails, file.getOriginalFilename());

        //assert
        assertEquals("Updated Title", updatedBook.getTitle());
        assertEquals("Updated Author", updatedBook.getAuthor());
        assertEquals("200.0", updatedBook.getPrice());
        assertEquals("new_image.jpg", updatedBook.getCoverImage());
        verify(bookRepository, times(1)).findById(1);
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void testUpdateBook_Success_WithoutFile() throws Exception {

        //arrange
        book = new Book();
        book.setId(1);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setPrice("100.0");
        book.setPublishDate("01/1999");

        Book newDetails = new Book();
        newDetails.setTitle("Updated Title");
        newDetails.setAuthor("Updated Author");
        newDetails.setPrice("200.0");
        newDetails.setPublishDate("12/2003");

        when(bookRepository.findById(anyInt())).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        //act
        Book updatedBook = bookService.updateBook(1, newDetails, null);

        //assert
        assertEquals("Updated Title", updatedBook.getTitle());
        assertEquals("Updated Author", updatedBook.getAuthor());
        assertEquals("200.0", updatedBook.getPrice());
        assertEquals("12/2003", updatedBook.getPublishDate());
        assertNull(updatedBook.getCoverImage());
        verify(bookRepository, times(1)).findById(1);
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void testUpdateBook_NotFound_Failure() throws Exception {

        //arrange
        Book newDetails = new Book();
        newDetails.setTitle("Updated Title");
        newDetails.setAuthor("Updated Author");
        newDetails.setPrice("200.0");
        newDetails.setPublishDate("12/2003");

        when(bookRepository.findById(anyInt())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookService.updateBook(1, newDetails, "new_image.jpg");
        });

        assertEquals("Book not found", exception.getMessage());
        verify(bookRepository, times(1)).findById(1);
        verify(bookRepository, times(0)).save(any(Book.class));
    }

    @Test
    void testDeleteBook_Success() {

        //arrange
        Book book = new Book();
        book.setId(4);

        when(bookRepository.findById(anyInt())).thenReturn(Optional.of(book));

        //act
        bookService.deleteBook(4);

        //assert
        verify(bookRepository, times(1)).findById(4);
        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void testDeleteBook_NotFound_Failure() {

        //arrange
        Book book = new Book();
        book.setId(6);

        when(bookRepository.findById(anyInt())).thenReturn(Optional.empty());

        //act & assert
        assertThrows(RuntimeException.class, () -> bookService.deleteBook(book.getId()), "Book not found");
    }
}
