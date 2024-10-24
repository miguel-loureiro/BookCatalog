package com.newbookcatalog.newbookcatalog.service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.newbookcatalog.newbookcatalog.exceptions.BookNotFoundException;
import com.newbookcatalog.newbookcatalog.exceptions.InvalidIsbnException;
import com.newbookcatalog.newbookcatalog.model.Role;
import com.newbookcatalog.newbookcatalog.model.User;
import com.newbookcatalog.newbookcatalog.model.dto.BookDetailWithoutUserListDto;
import com.newbookcatalog.newbookcatalog.model.dto.BookDto;
import com.newbookcatalog.newbookcatalog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.newbookcatalog.newbookcatalog.model.Book;
import com.newbookcatalog.newbookcatalog.repository.BookRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class BookService {

    @Autowired
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    //TODO: use in a future iteration where file will be uploaded to S3 storage
    //private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";
    //private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    @Autowired
    public BookService(UserRepository userRepository, BookRepository bookRepository, UserService userService) {

        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.userService = userService;
    }

    public ResponseEntity<Book> createBook(BookDetailWithoutUserListDto bookDto) {

        try {
            Optional<User> currentUserOpt = userService.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User currentUser = currentUserOpt.get();
            if (!hasPermissionToCreateOrUpdateBooks(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!isValidIsbn(bookDto.getIsbn())) {

                throw new InvalidIsbnException("Invalid ISBN number: " + bookDto.getIsbn(), null);
            }

            Book book = new Book();
            book.setTitle(bookDto.getTitle());
            book.setAuthor(bookDto.getAuthor());
            book.setIsbn(bookDto.getIsbn());
            book.setPrice(bookDto.getPrice());
            book.setPublishDate(bookDto.getPublishDate());
            book.setCoverImageUrl(bookDto.getCoverImageUrl());

            Book savedBook = bookRepository.save(book);
            return ResponseEntity.ok(savedBook);
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public Book createBook(Book book) {

        if (bookRepository == null) {

            throw new IllegalStateException("BookRepository is not initialized");
        }
        return bookRepository.save(book);
    }

    public Book getBookByIdentifier(String identifier, String type) {

        Optional<User> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            throw new IllegalStateException("Current user not found");
        }

        Book book;

        switch (type.toLowerCase()) {
            case "id":
                try {
                    book = bookRepository.getReferenceById(Integer.parseInt(identifier));
                } catch (EntityNotFoundException e) {
                    throw new BookNotFoundException("Book not found with id: " + identifier, e);
                }
                break;
            case "title":
                book = bookRepository.findBookByTitle(identifier)
                        .orElseThrow(() -> new BookNotFoundException("Book not found with title: " + identifier, null));
                break;
            case "isbn":
                book = bookRepository.findBookByIsbn(identifier)
                        .orElseThrow(() -> new BookNotFoundException("Book not found with ISBN: " + identifier, null));
                break;
            default:
                throw new IllegalArgumentException("Invalid identifier type: " + type);
        }
        return book;
    }

    public ResponseEntity<Page<BookDto>> getAllBooks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) throws IOException {

        Optional<User> currentUserOpt = userService.getCurrentUser();

        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = currentUserOpt.get();
        if (!hasPermissionToViewCompleteBooksWithUsers(currentUser)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Pageable paging = PageRequest.of(page, size, Sort.by("title").ascending());
        Page<Book> booksPage = bookRepository.findAll(paging);

        Page<BookDto> bookDtos = booksPage.map(book -> new BookDto(book));

        return ResponseEntity.ok(bookDtos);
    }

    public BookDto getBookWithShortUserDetails(String identifier, String type) {

        Book book = getBookByIdentifier(identifier, type);


        BookDto bookDto = new BookDto(book);


        return bookDto;
    }

    public ResponseEntity<Set<BookDetailWithoutUserListDto>> getOnlyBooks(int page, int size) {

        Optional<User> currentUserOpt = userService.getCurrentUser();

        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = currentUserOpt.get();

        if (!hasPermissionToViewBooksOnly(currentUser)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Pageable paging = PageRequest.of(page, size);
        Page<Book> booksPage = bookRepository.findAll(paging);

        Set<BookDetailWithoutUserListDto> bookOnlyDtos = booksPage.stream()
                .map(BookDetailWithoutUserListDto::new)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(bookOnlyDtos);
    }

    public Page<BookDetailWithoutUserListDto> getBooksByUserId(Integer userId, int page, int size) {

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return Page.empty(PageRequest.of(page, size));
        }

        User user = userOptional.get();
        List<Book> booksList = new ArrayList<>(user.getBooks());

        Page<Book> bookPage = paginateBooks(booksList, page, size);

        return bookPage.map(BookDetailWithoutUserListDto::new);
    }

    public Page<BookDetailWithoutUserListDto> getBooksByUserIdentifier(String identifier, int page, int size) {

        Optional<User> userOptional = userRepository.findByUsername(identifier);

        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(identifier);
        }

        return userOptional.map(user -> {

            List<Book> booksList = new ArrayList<>(user.getBooks());

            Page<Book> bookPage = paginateBooks(booksList, page, size);

            return bookPage.map(BookDetailWithoutUserListDto::new);

        }).orElse(Page.empty(PageRequest.of(page, size)));
    }

    public ResponseEntity<Book> updateBook(String identifier, String type, Book newBookDetails) throws IOException {

        Optional<User> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = currentUserOpt.get();

        if (!hasPermissionToCreateOrUpdateBooks(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {

            Book existingBook = getBookByIdentifier(identifier, type);

            if (!isValidIsbn(newBookDetails.getIsbn())) {
                throw new InvalidIsbnException("Invalid ISBN number: " + newBookDetails.getIsbn(), null);
            }

            newBookDetails.setId(existingBook.getId());

            bookRepository.save(newBookDetails);

            return ResponseEntity.ok(newBookDetails);
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public ResponseEntity<Void> deleteBook(String identifier, String type) {

        Optional<User> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = currentUserOpt.get();
        if (!hasPermissionToDeleteBooks(currentUser)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {

            Book bookToDelete = getBookByIdentifier(identifier, type);
            bookRepository.delete(bookToDelete);
            return ResponseEntity.ok().build();
        } catch (BookNotFoundException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public void saveAll(List<Book> books) {

        bookRepository.saveAll(books);
    }

    @Transactional
    public ResponseEntity<Object> addBookToCurrentUser(String identifier, String type) {

        try {

            Optional<User> currentUserOpt = userService.getCurrentUser();
            if (currentUserOpt.isEmpty()) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User currentUser = currentUserOpt.get();

            Book bookToAdd = getBookByIdentifier(identifier, type);

            Set<Book> userBooks = currentUser.getBooks();

            if (userBooks.contains(bookToAdd)) {

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Book already exists in user's collection");
            }

            Long initialUserVersion = currentUser.getVersion();
            Long initialBookVersion = bookToAdd.getVersion();

            userBooks.add(bookToAdd);
            currentUser.setBooks(userBooks);

            userRepository.save(currentUser);

            if (!Objects.equals(initialUserVersion, currentUser.getVersion()) ||
                    !Objects.equals(initialBookVersion, bookToAdd.getVersion())) {

                throw new OptimisticLockingFailureException("Failed due to concurrent modification.");
            }

            return ResponseEntity.ok("Book successfully added to user's collection");

        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (OptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict occurred due to concurrent modification.");

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<Object> deleteBookFromCurrentUser(String identifier, String type) {

        try {

            Optional<User> currentUserOpt = userService.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User currentUser = currentUserOpt.get();

            Book bookToDelete = getBookByIdentifier(identifier, type);

            Set<Book> userBooks = currentUser.getBooks();
            if (!userBooks.contains(bookToDelete)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Long initialUserVersion = currentUser.getVersion();
            Long initialBookVersion = bookToDelete.getVersion();

            userBooks.remove(bookToDelete);
            currentUser.setBooks(userBooks);

            userRepository.save(currentUser);

            if (!Objects.equals(initialUserVersion, currentUser.getVersion()) ||
                    !Objects.equals(initialBookVersion, bookToDelete.getVersion())) {

                throw new OptimisticLockingFailureException("Failed due to concurrent modification.");
            }

            return ResponseEntity.ok().build();

        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (OptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isValidIsbn(String isbn) {

        return isbn != null && (isValidIsbn10(isbn) || isValidIsbn13(isbn));
    }

    private boolean isValidIsbn10(String isbn) {
        if (isbn.length() != 10) return false;

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            if (!Character.isDigit(isbn.charAt(i))) return false;
            sum += (isbn.charAt(i) - '0') * (10 - i);
        }

        char checksum = isbn.charAt(9);
        sum += (checksum == 'X') ? 10 : (checksum - '0');

        return sum % 11 == 0;
    }

    private boolean isValidIsbn13(String isbn) {
        if (isbn.length() != 13) return false;

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = isbn.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }

        int checksum = 10 - (sum % 10);
        if (checksum == 10) checksum = 0;

        return checksum == (isbn.charAt(12) - '0');
    }

    private Page<Book> paginateBooks(List<Book> books, int page, int size) {

        Sort authorSort = Sort.by("author");
        Sort titleSort = Sort.by("title");
        Sort groupBySort = authorSort.and(titleSort);

        Pageable paging = PageRequest.of(page, size, groupBySort.ascending());

        int start = (int) paging.getOffset();
        int end = Math.min((start + paging.getPageSize()), books.size());

        if (start > books.size()) {

            return new PageImpl<>(Collections.emptyList(), paging, books.size());
        }

        List<Book> subList = books.subList(start, end);
        return new PageImpl<>(subList, paging, books.size());
    }

    private boolean hasPermissionToViewCompleteBooksWithUsers(User currentUser) {

        return currentUser.getRole() == Role.SUPER || currentUser.getRole() == Role.ADMIN;
    }

    private boolean hasPermissionToViewBooksOnly(User currentUser) {

        return currentUser.getRole() == Role.READER||
                currentUser.getRole() == Role.GUEST;
    }

    private boolean hasPermissionToDeleteBooks(User currentUser) {

        return currentUser.getRole() == Role.SUPER || currentUser.getRole() == Role.ADMIN;
    }

    private boolean hasPermissionToCreateOrUpdateBooks(User currentUser) {

        return currentUser.getRole() == Role.SUPER || currentUser.getRole() == Role.ADMIN;
    }
}
