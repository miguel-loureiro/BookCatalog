package com.bookcatalog.bookcatalog.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.bookcatalog.bookcatalog.exceptions.BookNotFoundException;
import com.bookcatalog.bookcatalog.helpers.FileUploadHelper;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.BookDetailWithoutUserListDto;
import com.bookcatalog.bookcatalog.model.dto.BookDto;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BookService {

    @Autowired
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    @Autowired
    public BookService(UserRepository userRepository, BookRepository bookRepository, UserService userService) {

        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.userService = userService;
    }

    public ResponseEntity<Book> createBook(BookDetailWithoutUserListDto bookDto, MultipartFile file) throws IOException {

        try {
            Optional<User> currentUserOpt = userService.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User currentUser = currentUserOpt.get();
            if (!hasPermissionToCreateOrUpdateBooks(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Book book = new Book();
            book.setTitle(bookDto.getTitle());
            book.setAuthor(bookDto.getAuthor());
            book.setIsbn(bookDto.getIsbn());
            book.setPrice(bookDto.getPrice());
            book.setPublishDate(bookDto.getPublishDate());


            if (file != null && !file.isEmpty()) {
                try {
                    String fileName = FileUploadHelper.saveFile(file);
                    book.setCoverImage(fileName);  // Set the cover image
                } catch (IOException e) {
                    return ResponseEntity.badRequest().eTag("Failed to upload").build();
                }
            } else {
                book.setCoverImage(null);
            }

            Book savedBook = bookRepository.save(book);
            return ResponseEntity.ok(savedBook);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public Book createBook(BookDetailWithoutUserListDto bookDto) throws IOException {

        Book book = new Book();
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setIsbn(bookDto.getIsbn());
        book.setPrice(bookDto.getPrice());
        book.setPublishDate((bookDto.getPublishDate()));
        book.setCoverImage(bookDto.getCoverImage());

        return bookRepository.save(book);
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

    // TODO: remove this method.
    public Page<Book> getBooksByAuthor(String author, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findBooksByAuthor(author, pageable);
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

        Pageable paging = PageRequest.of(page, size, Sort.by("author").and(Sort.by("title")).ascending());
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

    public ResponseEntity<Book> updateBook(String identifier, String type, Book newBookDetails, MultipartFile file) throws IOException {

        Optional<User> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = currentUserOpt.get();

        if (!hasPermissionToCreateOrUpdateBooks(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Book existingBook = getBookByIdentifier(identifier, type);
        if (existingBook == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        newBookDetails.setId(existingBook.getId());

        if (file != null && !file.isEmpty()) {

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().build();
            }

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filepath = Paths.get(UPLOAD_DIR, filename);
            Files.createDirectories(filepath.getParent());
            Files.write(filepath, file.getBytes());
            newBookDetails.setCoverImage(filename);
        }

        bookRepository.save(newBookDetails);

        return ResponseEntity.ok(newBookDetails);
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

        Optional<Book> bookToDeleteOpt = Optional.of(getBookByIdentifier(identifier, type));

        Book bookToDelete = bookToDeleteOpt.get();
        bookRepository.delete(bookToDelete);

        return ResponseEntity.ok().build();
    }

    public void saveAll(List<Book> books) {

        bookRepository.saveAll(books);
    }

    public ResponseEntity<Object> addBookToCurrentUser(String identifier, String type) {

        try {

            Optional<User> currentUserOpt = userService.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Current user not found");
            }

            User currentUser = currentUserOpt.get();

            Book book;
            try {
                book = getBookByIdentifier(identifier, type);
            } catch (BookNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }

            if (currentUser.getBooks().add(book)) {
                UserDto updatedUserDto = new UserDto(currentUser);

                try {

                    userService.updateUser(currentUser.getUsername(), "username", updatedUserDto);
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to update user after adding book: " + e.getMessage());
                }

                return ResponseEntity.ok().build();
            } else {

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Book already in user's collection");
            }
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    public ResponseEntity<Object> deleteBookFromCurrentUser(String identifier, String type) {

        try {

            Optional<User> currentUserOpt = userService.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Current user not found");
            }

            User currentUser = currentUserOpt.get();

            Book book;
            try {
                book = getBookByIdentifier(identifier, type);
            } catch (BookNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }

            if (currentUser.getBooks().remove(book)) {

                UserDto updatedUserDto = new UserDto(currentUser);

                try {

                    userService.updateUser(currentUser.getUsername(), "username", updatedUserDto);
                } catch (IOException e) {

                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to update user after removing book: " + e.getMessage());
                }

                return ResponseEntity.noContent().build();
            } else {

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found in user's collection");
            }
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
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

    private void updateBookDetails(Book bookToUpdate, Book newBookDetails, MultipartFile file) throws IOException {

        bookToUpdate.setTitle(newBookDetails.getTitle());
        bookToUpdate.setAuthor(newBookDetails.getAuthor());
        bookToUpdate.setPrice(newBookDetails.getPrice());
        bookToUpdate.setIsbn(newBookDetails.getIsbn());
        bookToUpdate.setPublishDate(newBookDetails.getPublishDate());

        if (file != null && !file.isEmpty()) {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IOException("File size exceeds the limit");
            }

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filepath = Paths.get(UPLOAD_DIR, filename);
            Files.createDirectories(filepath.getParent());
            Files.write(filepath, file.getBytes());
            bookToUpdate.setCoverImage(filename);
        }
        }

    private boolean hasPermissionToViewCompleteBooksWithUsers(User currentUser) {

        return currentUser.getRole() == Role.SUPER ||
                currentUser.getRole() == Role.ADMIN;
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
