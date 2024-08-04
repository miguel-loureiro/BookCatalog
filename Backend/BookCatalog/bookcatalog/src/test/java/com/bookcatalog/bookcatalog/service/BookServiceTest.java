package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.helpers.DateHelper;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.BookShortDto;
import com.bookcatalog.bookcatalog.model.dto.BookTitleAndAuthorDto;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockCurrentUser(UserDto userDto) {

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(userDto.getUsername());

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.of(mockUser(userDto)));
    }

    private User mockUser(UserDto userDto) {

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setRole(userDto.getRole());
        return user;
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
    void testGetBookById_Success() {

        //Arrange
        Integer bookId = 8;
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");

        Book savedBook = new Book();
        savedBook.setId(bookId);
        savedBook.setTitle("Test Book");
        savedBook.setAuthor("Test Author");

        when(bookRepository.findById(8)).thenReturn(Optional.of(book));

        //Act
        Optional<Book> foundBook = bookService.getBookById(bookId);

        //Assert
        assertTrue(foundBook.isPresent());
        assertEquals("Test Book", foundBook.get().getTitle());
        assertEquals("Test Author", foundBook.get().getAuthor());
    }
/*
    @Test
    public void testGetAllBooks_UserSuperOrAdmin_Success() {

        // Arrange
        UserDto user = new UserDto();
        user.setRole(Role.SUPER);
        mockCurrentUser(user);

        List<Book> books = Arrays.asList(new Book(), new Book());
        when(bookRepository.findAll()).thenReturn(books);

        // Act
        ResponseEntity<List<Book>> response = bookService.getAllBooks();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
    }

    @Test
    public void testGetAllBooks_UserReaderOrGuest_Failure() {

        // Arrange
        UserDto userDto = new UserDto();
        userDto.setUsername("user");
        userDto.setRole(Role.READER);
        mockCurrentUser(userDto);

        // Act
        ResponseEntity<List<Book>> response = bookService.getAllBooks();

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testGetAllBooks_UserIsNull() {

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);

        ResponseEntity<List<Book>> response = bookService.getAllBooks();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testGetAllBooks_UserRoleIsNull_Failure() {

        // Arrange
        UserDto userDto = new UserDto();
        userDto.setUsername("user");
        userDto.setRole(null); // Role is null
        mockCurrentUser(userDto);

        // Act
        ResponseEntity<List<Book>> response = bookService.getAllBooks();

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
*/
    @Test
    public void testGetAllBooksShort_UserSuperOrAdmin_Success() {

        // Arrange
        UserDto user = new UserDto();
        user.setRole(Role.ADMIN);
        mockCurrentUser(user);

        List<Book> books = Arrays.asList(new Book(), new Book());
        when(bookRepository.findAll()).thenReturn(books);

        // Act
        ResponseEntity<List<BookShortDto>> response = bookService.getAllBooksShort();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
    }

    @Test
    public void testGetAllBooksShort_UserReaderOrGuest_Failure() {

        // Arrange
        UserDto userDto = new UserDto();
        userDto.setUsername("user");
        userDto.setRole(Role.READER);
        mockCurrentUser(userDto);

        // Act
        ResponseEntity<List<BookShortDto>> response = bookService.getAllBooksShort();

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testGetAllBooksShort_UserIsNull() {

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);

        List<Book> books = Arrays.asList(new Book(), new Book());
        when(bookRepository.findAll()).thenReturn(books);

        ResponseEntity<List<BookShortDto>> response = bookService.getAllBooksShort();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

    }

    @Test
    public void testGetAllBooksShort_UserRoleIsNull_Failure() {

        // Arrange
        UserDto userDto = new UserDto();
        userDto.setUsername("user");
        userDto.setRole(null); // Role is null
        mockCurrentUser(userDto);

        List<Book> books = Arrays.asList(new Book(), new Book());
        when(bookRepository.findAll()).thenReturn(books);

        // Act
        ResponseEntity<List<BookShortDto>> response = bookService.getAllBooksShort();

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testGetAllBooksShort_Success() throws ParseException, IOException {
        // Arrange
        UserDto user = new UserDto();
        user.setRole(Role.ADMIN);
        mockCurrentUser(user);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");
        Date dateInDateFormat = dateFormat.parse("04/1975");
        String dateInString = DateHelper.serialize(dateInDateFormat);

        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setIsbn("ISBN");
        book.setPrice("10.0");
        book.setPublishDate(dateInString );
        when(bookRepository.findAll()).thenReturn(Collections.singletonList(book));

        // Act
        ResponseEntity<List<BookShortDto>> response = bookService.getAllBooksShort();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        BookShortDto dto = response.getBody().get(0);
        assertEquals("Title", dto.getTitle());
        assertEquals("Author", dto.getAuthor());
        assertEquals("ISBN", dto.getIsbn());
        assertEquals("10.0", dto.getPrice());
        assertNotNull(dto.getPublishDate());
    }

    @Test
    public void testGetAllBooksShort_NullBooks() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setUsername("user");
        userDto.setRole(Role.ADMIN); // Role is null
        mockCurrentUser(userDto);

        when(bookRepository.findAll()).thenReturn(null);

        // Act
        ResponseEntity<List<BookShortDto>> response = bookService.getAllBooksShort();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testGetAllBooksShort_EmptyBooksList() {
        // Arrange
        UserDto user = new UserDto();
        user.setRole(Role.ADMIN);
        user.setRole(Role.ADMIN); // Role is null
        mockCurrentUser(user);

        when(bookRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<BookShortDto>> response = bookService.getAllBooksShort();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
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
        books.add(new Book(1, "Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book(2, "Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        Set<BookShortDto> result = bookService.getBooksByUserId(userId);

        // Assert
        assertEquals(2, result.size());

        Book book1 = books.stream().filter(book -> book.getTitle().equals("Title1")).findFirst().orElse(null);
        Book book2 = books.stream().filter(book -> book.getTitle().equals("Title2")).findFirst().orElse(null);

        BookShortDto dto1 = result.stream().filter(dto -> dto.getTitle().equals("Title1")).findFirst().orElse(null);
        BookShortDto dto2 = result.stream().filter(dto -> dto.getTitle().equals("Title2")).findFirst().orElse(null);

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
    public void testGetBooksByUserId_UserNotFound() {
        // Arrange
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        Set<BookShortDto> result = bookService.getBooksByUserId(userId);

        // Assert
        assertTrue(result.isEmpty());
    }


    @Test
    public void testGetBooksByUserIdentifier_UserNotFound() {
        // Arrange
        String identifier = "nonexistent";
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        // Act
        Set<BookShortDto> result = bookService.getBooksByUserIdentifier(identifier);

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
        books.add(new Book(1, "Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book(2, "Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        // Act
        Set<BookShortDto> result = bookService.getBooksByUserIdentifier(identifier);

        // Print the result to console
        System.out.println(result);

        // Assert the size
        assertEquals(2, result.size());

        // Assert the details of each book
        Book book1 = books.stream().filter(book -> book.getTitle().equals("Title1")).findFirst().orElse(null);
        Book book2 = books.stream().filter(book -> book.getTitle().equals("Title2")).findFirst().orElse(null);

        BookShortDto dto1 = result.stream().filter(dto -> dto.getTitle().equals("Title1")).findFirst().orElse(null);
        BookShortDto dto2 = result.stream().filter(dto -> dto.getTitle().equals("Title2")).findFirst().orElse(null);

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
        books.add(new Book(1, "Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book(2, "Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(user));

        // Act
        Set<BookShortDto> result = bookService.getBooksByUserIdentifier(identifier);

        // Print the result to console
        System.out.println(result);

        // Assert the size
        assertEquals(2, result.size());

        // Assert the details of each book
        Book book1 = books.stream().filter(book -> book.getTitle().equals("Title1")).findFirst().orElse(null);
        Book book2 = books.stream().filter(book -> book.getTitle().equals("Title2")).findFirst().orElse(null);

        BookShortDto dto1 = result.stream().filter(dto -> dto.getTitle().equals("Title1")).findFirst().orElse(null);
        BookShortDto dto2 = result.stream().filter(dto -> dto.getTitle().equals("Title2")).findFirst().orElse(null);

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
    public void testGetAllBooksTitlesAndAuthors() {
        // Arrange
        Book book1 = new Book(1, "Title1", "Author1", "ISBN1", "10.0", null, null, null);
        Book book2 = new Book(2, "Title2", "Author2", "ISBN2", "20.0", null, null, null);
        List<Book> books = Arrays.asList(book1, book2);
        when(bookRepository.findAll()).thenReturn(books);

        // Act
        List<BookTitleAndAuthorDto> result = bookService.getAllBooksTitlesAndAuthors();

        // Print the result to console
        System.out.println(result);

        // Assert
        assertEquals(2, result.size());

        // Assert Book 1
        BookTitleAndAuthorDto dto1 = result.get(0);
        assertEquals("Title1", dto1.getTitle());
        assertEquals("Author1", dto1.getAuthor());

        // Assert Book 2
        BookTitleAndAuthorDto dto2 = result.get(1);
        assertEquals("Title2", dto2.getTitle());
        assertEquals("Author2", dto2.getAuthor());
    }

    @Test
    public void testUpdateBook() throws IOException {
        // Arrange
        Integer id = 1;
        Book existingBook = new Book(id, "Old Title", "Old Author", "Old ISBN", "5.0", null, null, null);
        Book newDetails = new Book(null, "New Title", "New Author", "New ISBN", "15.0", null, null, null);
        String filename = "newCoverImage.jpg";

        when(bookRepository.findById(id)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenReturn(existingBook);

        // Act
        Book updatedBook = bookService.updateBook(id, newDetails, filename);

        // Print the result to console
        System.out.println(updatedBook);

        // Assert
        assertEquals("New Title", updatedBook.getTitle());
        assertEquals("New Author", updatedBook.getAuthor());
        assertEquals("New ISBN", updatedBook.getIsbn());
        assertEquals("15.0", updatedBook.getPrice());
        assertEquals(filename, updatedBook.getCoverImage());
    }

    @Test
    public void testUpdateBook_NoFilename() throws IOException {
        // Arrange
        Integer id = 1;
        Book existingBook = new Book(id, "Old Title", "Old Author", "Old ISBN", "5.0", null, null, null);
        Book newDetails = new Book(null, "New Title", "New Author", "New ISBN", "15.0", null, null, null);

        when(bookRepository.findById(id)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenReturn(existingBook);

        // Act
        Book updatedBook = bookService.updateBook(id, newDetails, null);

        // Print the result to console
        System.out.println(updatedBook);

        // Assert
        assertEquals("New Title", updatedBook.getTitle());
        assertEquals("New Author", updatedBook.getAuthor());
        assertEquals("New ISBN", updatedBook.getIsbn());
        assertEquals("15.0", updatedBook.getPrice());
        assertNull(updatedBook.getCoverImage());
    }

    @Test
    public void testDeleteBookById() {

        // Arrange
        Integer id = 1;
        Book book = new Book(id, "Title", "Author", "ISBN", "10.0", null, null, null);
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        // Act
        bookService.deleteBookById(id);

        // Verify
        verify(bookRepository).delete(book);
    }

    @Test
    public void testDeleteBookById_BookNotFound() {

        // Arrange

        when(bookRepository.findById(1234)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException result = assertThrows(RuntimeException.class, () -> {
            bookService.deleteBookById(1234);
        });
        assertEquals("Book not found", result.getMessage());
    }

    @Test
    public void testSaveAll() {
        // Arrange
        List<Book> books = Arrays.asList(
                new Book(1, "Title1", "Author1", "ISBN1", "10.0", null, null, null),
                new Book(2, "Title2", "Author2", "ISBN2", "20.0", null, null, null)
        );

        // Act
        bookService.saveAll(books);

        // Verify
        verify(bookRepository).saveAll(books);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
