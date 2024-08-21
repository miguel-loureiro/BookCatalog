package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.exceptions.BookNotFoundException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteBookByISBNStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteBookByIdStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteBookByTitleStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateBookByISBNStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateBookByIdStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateBookByTitleStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateStrategy;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private Book mockBook;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @Mock
    private User mockUser;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UpdateBookByIdStrategy updateBookByIdStrategy;

    @Mock
    private UpdateBookByTitleStrategy updateBookByTitleStrategy;

    @Mock
    private DeleteBookByIdStrategy deleteBookByIdStrategy;

    @Mock
    private DeleteBookByTitleStrategy deleteBookByTitleStrategy;

    @Mock
    private DeleteBookByISBNStrategy deleteBookByISBNStrategy;

    @Mock
    private UpdateBookByISBNStrategy updateBookByISBNStrategy;

    private BookService bookService; // removi isto do injectMock pois tenho 2 construtores diferentes e o injectMocks usa o que tem todos os argumentos
    private Book book;
    private Book newDetails;

    @BeforeEach
    public void setUp() {

        MockitoAnnotations.openMocks(this);

        List<UpdateStrategy> updatestrategies = List.of(updateBookByIdStrategy, updateBookByTitleStrategy, updateBookByISBNStrategy);
        List<DeleteStrategy> deletestrategies = List.of(deleteBookByIdStrategy, deleteBookByTitleStrategy, deleteBookByISBNStrategy);
        bookService = new BookService(userRepository, bookRepository, updatestrategies, deletestrategies);
        SecurityContextHolder.setContext(securityContext);

        book = new Book(1, "Title", "Author");
        newDetails = new Book(1, "New Title", "New Author");
    }

    private void mockCurrentUser(User user) {

        if (user == null) {

            SecurityContextHolder.clearContext();
            when(securityContext.getAuthentication()).thenReturn(null);

        } else {

            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getUsername()).thenReturn(user.getUsername());

            SecurityContextHolder.setContext(securityContext);

            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        }
    }

    @Test
    void testCreateBook_Success() {

        // Arrange
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");

        Book savedBook = new Book();
        savedBook.setId(1);
        savedBook.setTitle("Test Book");
        savedBook.setAuthor("Test Author");

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        // Act
        Book result = bookService.createBook(book);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Book", result.getTitle());
        assertEquals("Test Author", result.getAuthor());

        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void testCreateBookWithNullBookRepository_Failure() {

        // Arrange
        bookService = new BookService(userRepository, null);

        // Act & Assert
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            bookService.createBook(book);
        });

        assertEquals("BookRepository is not initialized", exception.getMessage());
    }

    @Test
    public void testGetBook() {

    }

    @Test
    public void testGetBooksByAuthor_BooksFound() {
        // Arrange
        String author = "Author";
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        List<Book> books = List.of(
                new Book("Title1", author, "1234567890", "10.99", null, null, null),
                new Book("Title2", author, "0987654321", "12.99", null, null, null)
        );

        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findBooksByAuthor(author, pageable)).thenReturn(bookPage);

        // Act
        Page<Book> result = bookService.getBooksByAuthor(author, page, size);

        // Assert
        assertEquals(books.size(), result.getTotalElements());
        assertEquals(books, result.getContent());
        verify(bookRepository, times(1)).findBooksByAuthor(author, pageable);
    }

    @Test
    public void testGetBooksByAuthor_NoBooksFound() {
        // Arrange
        String author = "Author";
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(bookRepository.findBooksByAuthor(author, pageable)).thenReturn(emptyPage);

        // Act
        Page<Book> result = bookService.getBooksByAuthor(author, page, size);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(bookRepository, times(1)).findBooksByAuthor(author, pageable);
    }

    @Test
    public void testGetAllBooks_UserSuperOrAdmin_Success() throws IOException {

        // Arrange
        User user = new User();
        user.setRole(Role.SUPER);
        mockCurrentUser(user);

        Book book1 = new Book("Title1", "Author1", "000000000000", "10.00", new Date(), "cover1.jpg");
        Book book2 = new Book("Title2", "Author2", "000000000001", "15.00", new Date(), "cover2.jpg");
        List<Book> books = Arrays.asList(book1, book2);
        Page<Book> booksPage = new PageImpl<>(books);

        when(bookRepository.findAll(any(Pageable.class))).thenReturn(booksPage);

        // Act
        ResponseEntity<Page<Book>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).getTotalElements());
    }

    @Test
    public void testGetAllBooks_UserReader_Failure() throws IOException {

        // Arrange
        User user = new User();
        user.setUsername("user");
        user.setRole(Role.READER);
        mockCurrentUser(user);

        // Act
        ResponseEntity<Page<Book>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testGetAllBooks_UserGuest_Failure() throws IOException {

        // Arrange
        User user = new User();
        user.setUsername("user");
        user.setRole(Role.GUEST);
        mockCurrentUser(user);

        // Act
        ResponseEntity<Page<Book>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testGetAllBooks_UserIsNull() throws IOException {

        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
        // Act
        ResponseEntity<Page<Book>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(bookRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    public void testGetAllBooks_UserRoleIsNull_Failure() throws IOException {

        // Arrange
        User user = new User();
        user.setUsername("user");
        user.setRole(null);
        mockCurrentUser(user);

        // Act
        ResponseEntity<Page<Book>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

    }

    @Test
    public void testGetAllBooks_ThrowsException() {

        // Arrange
        User user = new User();
        user.setUsername("user");
        user.setRole(Role.ADMIN);
        mockCurrentUser(user);

        // Act
        when(bookRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("An error occurred while fetching books"));
        // Assert


    }

    @Test
    public void testGetBooksByUserId_UserFound() throws ParseException {
        // Arrange
        Integer userId = 1;
        User user = new User();
        Set<User> users = new HashSet<>();
        users.add(user);

        Set<Book> books = new HashSet<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date1 = dateFormat.parse("23-08-2001");
        Date date2 = dateFormat.parse("07-12-1978");
        books.add(new Book("Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book("Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        Page<Book> result = bookService.getBooksByUserId(userId,0 , 10);

        // Assert
        assertEquals(2, result.getTotalElements());

        Book book1 = books.stream().filter(book -> book.getTitle().equals("Title1")).findFirst().orElse(null);
        Book book2 = books.stream().filter(book -> book.getTitle().equals("Title2")).findFirst().orElse(null);

        Book dto1 = result.stream().filter(dto -> dto.getTitle().equals("Title1")).findFirst().orElse(null);
        Book dto2 = result.stream().filter(dto -> dto.getTitle().equals("Title2")).findFirst().orElse(null);

        assertNotNull(book1);
        assertNotNull(book2);
        assertNotNull(dto1);
        assertNotNull(dto2);

        assertEquals(book1.getTitle(), dto1.getTitle());
        assertEquals(book1.getAuthor(), dto1.getAuthor());
        assertEquals(book1.getIsbn(), dto1.getIsbn());
        assertEquals(book1.getPublishDate(), dto1.getPublishDate());
        assertEquals(book1.getPrice(), dto1.getPrice());

        assertEquals(book2.getTitle(), dto2.getTitle());
        assertEquals(book2.getAuthor(), dto2.getAuthor());
        assertEquals(book2.getIsbn(), dto2.getIsbn());
        assertEquals(book2.getPublishDate(), dto2.getPublishDate());
        assertEquals(book2.getPrice(), dto2.getPrice());
    }

    @Test
    public void testGetBooksByUserId_UserFound_StartPageIsGreater_ReturnsEmptyList() throws ParseException {
        // Arrange
        Integer userId = 1;
        User user = new User();
        Set<User> users = new HashSet<>();
        users.add(user);

        Set<Book> books = new HashSet<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date1 = dateFormat.parse("23-08-2001");
        Date date2 = dateFormat.parse("07-12-1978");
        books.add(new Book("Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book("Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        Page<Book> result = bookService.getBooksByUserId(userId,2 , 10);

        // Assert
        assertTrue(result.getContent().isEmpty());
        assertEquals(books.size(), result.getTotalElements());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void testGetBooksByUserId_UserNotFound() {
        // Arrange
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        Page<Book> result = bookService.getBooksByUserId(userId, 0, 10);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetBooksByUserIdentifier_UserFoundByUsername() throws ParseException {
        // Arrange
        String identifier = "username";
        User user = new User();
        Set<User> users = new HashSet<>();
        users.add(user);

        Set<Book> books = new HashSet<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date1 = dateFormat.parse("23-08-2001");
        Date date2 = dateFormat.parse("07-12-1978");
        books.add(new Book("Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book("Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        // Act
        Page<Book> result = bookService.getBooksByUserIdentifier(identifier, 0, 10);

        // Assert the size
        assertEquals(2, result.getTotalElements());

        // Assert the details of each book
        Book book1 = books.stream().filter(book -> book.getTitle().equals("Title1")).findFirst().orElse(null);
        Book book2 = books.stream().filter(book -> book.getTitle().equals("Title2")).findFirst().orElse(null);

        Book dto1 = result.stream().filter(dto -> dto.getTitle().equals("Title1")).findFirst().orElse(null);
        Book dto2 = result.stream().filter(dto -> dto.getTitle().equals("Title2")).findFirst().orElse(null);

        assertNotNull(book1);
        assertNotNull(book2);
        assertNotNull(dto1);
        assertNotNull(dto2);

        // Assert book1 and dto1 details
        assertEquals(book1.getTitle(), dto1.getTitle());
        assertEquals(book1.getAuthor(), dto1.getAuthor());
        assertEquals(book1.getIsbn(), dto1.getIsbn());
        assertEquals(book1.getPublishDate(), dto1.getPublishDate());
        assertEquals(book1.getPrice(), dto1.getPrice());

        // Assert book2 and dto2 details
        assertEquals(book2.getTitle(), dto2.getTitle());
        assertEquals(book2.getAuthor(), dto2.getAuthor());
        assertEquals(book2.getIsbn(), dto2.getIsbn());
        assertEquals(book2.getPublishDate(), dto2.getPublishDate());
        assertEquals(book2.getPrice(), dto2.getPrice());
    }

    @Test
    public void testGetBooksByUserIdentifier_UserFoundByUsername_StartPageIsGreater_ReturnsEmptyList() throws ParseException {
        // Arrange
        String identifier = "username";
        User user = new User();
        Set<User> users = new HashSet<>();
        users.add(user);

        Set<Book> books = new HashSet<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date1 = dateFormat.parse("23-08-2001");
        Date date2 = dateFormat.parse("07-12-1978");
        books.add(new Book("Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book("Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        // Act
        Page<Book> result = bookService.getBooksByUserIdentifier(identifier, 2, 10);

        // Assert
        assertTrue(result.getContent().isEmpty());
        assertEquals(books.size(), result.getTotalElements());
        verify(userRepository, times(1)).findByUsername(identifier);
    }

    @Test
    public void testGetBooksByUserIdentifier_UserFoundByEmail() throws ParseException {
        // Arrange
        String identifier = "email@example.com";
        User user = new User();
        Set<User> users = new HashSet<>();
        users.add(user);

        Set<Book> books = new HashSet<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date1 = dateFormat.parse("23-08-2001");
        Date date2 = dateFormat.parse("07-12-1978");
        books.add(new Book("Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book("Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(user));

        // Act
        Page<Book> result = bookService.getBooksByUserIdentifier(identifier, 0, 10);

        // Print the result to console
        System.out.println(result);

        // Assert the size
        assertEquals(2, result.getTotalElements());

        // Assert the details of each book
        Book book1 = books.stream().filter(book -> book.getTitle().equals("Title1")).findFirst().orElse(null);
        Book book2 = books.stream().filter(book -> book.getTitle().equals("Title2")).findFirst().orElse(null);

        Book dto1 = result.stream().filter(dto -> dto.getTitle().equals("Title1")).findFirst().orElse(null);
        Book dto2 = result.stream().filter(dto -> dto.getTitle().equals("Title2")).findFirst().orElse(null);

        assertNotNull(book1);
        assertNotNull(book2);
        assertNotNull(dto1);
        assertNotNull(dto2);

        // Assert book1 and dto1 details
        assertEquals(book1.getTitle(), dto1.getTitle());
        assertEquals(book1.getAuthor(), dto1.getAuthor());
        assertEquals(book1.getIsbn(), dto1.getIsbn());
        assertEquals(book1.getPublishDate(), dto1.getPublishDate());
        assertEquals(book1.getPrice(), dto1.getPrice());

        // Assert book2 and dto2 details
        assertEquals(book2.getTitle(), dto2.getTitle());
        assertEquals(book2.getAuthor(), dto2.getAuthor());
        assertEquals(book2.getIsbn(), dto2.getIsbn());
        assertEquals(book2.getPublishDate(), dto2.getPublishDate());
        assertEquals(book2.getPrice(), dto2.getPrice());
    }

    @Test
    public void testGetBooksByUserIdentifier_UserFoundByEmail_StartPageIsGreater_ReturnsEmptyList() throws ParseException {
        // Arrange
        String identifier = "email@example.com";
        User user = new User();
        Set<User> users = new HashSet<>();
        users.add(user);

        Set<Book> books = new HashSet<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date1 = dateFormat.parse("23-08-2001");
        Date date2 = dateFormat.parse("07-12-1978");
        books.add(new Book("Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book("Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(user));

        // Act
        Page<Book> result = bookService.getBooksByUserIdentifier(identifier, 2, 10);

        // Assert
        assertTrue(result.getContent().isEmpty());
        assertEquals(books.size(), result.getTotalElements());
        verify(userRepository, times(1)).findByEmail(identifier);
    }

    @Test
    public void testGetBooksByUserIdentifier_UserNotFound() {
        // Arrange
        String identifier = "nonexistent";
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        // Act
        Page<Book> result = bookService.getBooksByUserIdentifier(identifier, 0, 10);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testUpdateBookByBookId_Success() throws IOException {

        when(bookRepository.getReferenceById(1)).thenReturn(book);
        when(updateBookByIdStrategy.update(book, newDetails, "cover.jpg")).thenReturn(newDetails);

        // Act: Call the updateBook method
        Book updatedBook = bookService.updateBook("1", "id", newDetails, "cover.jpg");


        // Assert: Verify the updates and interactions
        assertEquals("New Title", updatedBook.getTitle());
        assertEquals("New Author", updatedBook.getAuthor());
        verify(bookRepository, times(1)).getReferenceById(1);
        verify(updateBookByIdStrategy, times(1)).update(book, newDetails, "cover.jpg");
    }

    @Test
    public void testUpdateBookByBookTitle_Success() throws IOException {

        when(bookRepository.findBookByTitle("Title")).thenReturn(Optional.of(book));
        when(updateBookByTitleStrategy.update(book, newDetails, "cover.jpg")).thenReturn(newDetails);

        // Act: Call the updateBook method
        Book updatedBook = bookService.updateBook("Title", "title", newDetails, "cover.jpg");

        // Assert: Verify the updates and interactions
        assertEquals("New Title", updatedBook.getTitle());
        assertEquals("New Author", updatedBook.getAuthor());
        verify(bookRepository, times(1)).findBookByTitle("Title");
        verify(updateBookByTitleStrategy, times(1)).update(book, newDetails, "cover.jpg");
    }

    @Test
    public void testUpdateBookByBookISBN_Success() throws IOException {

        String bookISBN = "1234567890123";
        String newDetailsISBN = "1234567890124";
        book.setIsbn(bookISBN);
        newDetails.setIsbn(newDetailsISBN);

        when(bookRepository.findBookByIsbn(bookISBN)).thenReturn(Optional.of(book));
        when(updateBookByISBNStrategy.update(book, newDetails, "cover.jpg")).thenReturn(newDetails);

        // Act: Call the updateBook method
        Book updatedBook = bookService.updateBook("1234567890123", "isbn", newDetails, "cover.jpg");

        // Assert: Verify the updates and interactions
        assertEquals("New Title", updatedBook.getTitle());
        assertEquals("New Author", updatedBook.getAuthor());
        assertEquals(newDetailsISBN, updatedBook.getIsbn());
        verify(bookRepository, times(1)).findBookByIsbn(bookISBN);
        verify(updateBookByISBNStrategy, times(1)).update(book, newDetails, "cover.jpg");
    }

    @Test
    public void testUpdateBook_InvalidUpdateType() {

        assertThrows(IllegalArgumentException.class, () -> {
            bookService.updateBook("1", "invalid_type", newDetails, "cover.jpg");
        });

        verify(bookRepository, never()).getReferenceById(anyInt());
        verify(bookRepository, never()).findBookByTitle(anyString());
        verify(bookRepository, never()).findBookByIsbn(anyString());
    }

    @Test
    public void testUpdateBook_BookNotFound() throws IOException {

        when(updateBookByIdStrategy.update(null, newDetails, "cover.jpg")).thenThrow(new EntityNotFoundException("Book not found"));

        assertThrows(EntityNotFoundException.class, () -> {
            bookService.updateBook("1", "id", newDetails, "cover.jpg");
        });
    }

    @Test
    public void testUpdateBook_NewDetailsNull() throws IOException {

        // Arrange
        when(bookRepository.getReferenceById(1)).thenReturn(book);
        when(updateBookByIdStrategy.update(book, null, "cover.jpg")).thenThrow(new IllegalArgumentException("New details cannot be null"));
        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> {
            bookService.updateBook("1", "id", null, "cover.jpg");
        }, "New details cannot be null");
    }

    @Test
    public void testDeleteBookById_Success() throws IOException {

        // Arrange
        when(bookRepository.getReferenceById(1)).thenReturn(book);

        // Act
        bookService.deleteBook("1", "id");

        // Assert
        verify(deleteBookByIdStrategy, times(1)).delete(book);
    }

    @Test
    public void testDeleteBookById_BookNotFound_Failure() throws IOException {

        // Arrange
        when(bookRepository.getReferenceById(anyInt())).thenThrow(new EntityNotFoundException("Book not found"));

        // Act and Assert
        assertThrows(BookNotFoundException.class, () -> {
            bookService.deleteBook("1", "id");
        });
    }

    @Test
    public void testDeleteBook_InvalidDeleteType() {

        assertThrows(IllegalArgumentException.class, () -> {
            bookService.deleteBook("1", "invalid_type");
        });

        verify(bookRepository, never()).getReferenceById(anyInt());
        verify(bookRepository, never()).findBookByTitle(anyString());
        verify(bookRepository, never()).findBookByIsbn(anyString());
    }

    @Test
    public void testDeleteBookByTitle_Success() throws IOException {

        // Arrange
        when(bookRepository.findBookByTitle("Title")).thenReturn(Optional.of(book));

        // Act
        bookService.deleteBook("Title", "title");

        // Assert
        verify(deleteBookByTitleStrategy, times(1)).delete(book);
    }

    @Test
    public void testDeleteBookByISBN_Success() throws IOException {

        // Arrange
        when(bookRepository.findBookByIsbn("000000000000")).thenReturn(Optional.of(book));

        // Act
        bookService.deleteBook("000000000000", "isbn");

        // Assert
        verify(deleteBookByISBNStrategy, times(1)).delete(book);
    }

    @Test
    void testAddBookToCurrentUser() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername("testuser");

        Book mockBook = new Book();
        mockBook.setId(1);
        mockBook.setTitle("Test Book");

        Set<Book> books = new HashSet<>();
        mockUser.setBooks(books);

        mockCurrentUser(mockUser);

        when(bookRepository.getReferenceById(1)).thenReturn(mockBook);

        // Act
        bookService.addBookToCurrentUser("1", "id");

        // Assert
        assertTrue(mockUser.getBooks().contains(mockBook), "The book should be added to the user's collection");
        verify(userRepository).save(mockUser);
    }

    @Test
    public void testAddBookToCurrentUser_BookAlreadyInCollection() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername("testuser");

        Book mockBook = new Book();
        mockBook.setId(1);
        mockBook.setTitle("Test Book");

        Set<Book> books = new HashSet<>();
        books.add(mockBook);
        mockUser.setBooks(books);

        mockCurrentUser(mockUser);

        when(bookRepository.getReferenceById(1)).thenReturn(mockBook);

        // Act
        bookService.addBookToCurrentUser("1", "id");

        // Assert
        assertEquals(1, mockUser.getBooks().size(), "The user's collection should not have duplicate books");
        assertTrue(mockUser.getBooks().contains(mockBook), "The user's collection should contain the book");
        verify(userRepository).save(mockUser);
    }

    @Test
    public void testAddBookToCurrentUser_UserNotAuthenticated() {
        // Arrange
        mockCurrentUser(null);

        // Act and Assert
        assertThrows(IllegalStateException.class, () -> {
            bookService.addBookToCurrentUser("1", "id");
        }, "Expected IllegalStateException to be thrown when no user is authenticated");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testDeleteBookFromCurrentUser_Success() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername("testuser");

        Book mockBook = new Book();
        mockBook.setId(1);
        mockBook.setTitle("Test Book");

        Set<Book> books = new HashSet<>();
        books.add(mockBook);
        mockUser.setBooks(books);

        mockCurrentUser(mockUser);

        when(bookRepository.getReferenceById(1)).thenReturn(mockBook);

        // Act
        bookService.deleteBookFromCurrentUser("1", "id");

        // Assert
        assertFalse(mockUser.getBooks().contains(mockBook), "The book should be removed from the user's collection");
        verify(userRepository).save(mockUser);
    }

    @Test
    public void testDeleteBookFromCurrentUser_BookNotFoundInCollection() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setBooks(new HashSet<>());

        mockCurrentUser(mockUser);

        Book mockBook = new Book();
        mockBook.setId(1);
        when(bookRepository.getReferenceById(1)).thenReturn(mockBook);

        // Act and Assert
        assertThrows(EntityNotFoundException.class, () -> {
            bookService.deleteBookFromCurrentUser("1", "id");
        }, "Expected EntityNotFoundException to be thrown when the book is not in the user's collection");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testSaveAll() {
        // Arrange
        List<Book> books = Arrays.asList(
                new Book("Title1", "Author1", "ISBN1", "10.0", null, null, null),
                new Book("Title2", "Author2", "ISBN2", "20.0", null, null, null)
        );

        // Act
        bookService.saveAll(books);

        // Verify
        verify(bookRepository).saveAll(books);
    }

private UserDetails mockUserDetails(String username) {
    return new org.springframework.security.core.userdetails.User(username, "password", new ArrayList<>());
}


@AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
