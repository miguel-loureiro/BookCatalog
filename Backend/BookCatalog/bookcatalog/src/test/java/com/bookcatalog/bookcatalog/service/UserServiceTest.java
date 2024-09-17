package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.model.CustomUserDetails;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.dto.BookTitleAndAuthorDto;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomUserDetails customUserDetails;

    @InjectMocks
    private UserService userService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User currentUser;
    private RegisterUserDto registerUserDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);

        currentUser = new User("super", "super@example.com", "password", Role.SUPER);
        User userToSave = new User("testUser", "test@example.com", "password", Role.READER);
        UserDto userDto = new UserDto("testUser", "test@example.com", Role.READER);
        registerUserDto = new RegisterUserDto("testUser", "test@example.com", "password", Role.READER);
    }

    private void mockCurrentUser(User user) {

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new CustomUserDetails(user));
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
        when(userRepository.findByIdWithBooks(user.getId())).thenReturn(Optional.of(user));
    }

    @Test
    public void testGetAllUsers_Success() {
        // Arrange
        Pageable paging = PageRequest.of(0, 10, Sort.by("username").ascending());
        Page<User> usersPage = new PageImpl<>(List.of(currentUser));
        when(userRepository.findAll(paging)).thenReturn(usersPage);

        // Act
        ResponseEntity<Page<UserDto>> response = userService.getAllUsers(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
    }

    @Test
    public void testCreateUser_NoAuthentication_Unauthorized() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto();
        input.setUsername("newUser");
        input.setEmail("newuser@example.com");
        input.setPassword("password123");
        input.setRole(Role.READER);

        // Simulate no authentication context
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(null);

        // Act
        ResponseEntity<UserDto> response = userService.createUser(input);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testCreateUser_PrincipalNotUserDetails_Unauthorized() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto();
        input.setUsername("newUser");
        input.setEmail("newuser@example.com");
        input.setPassword("password123");
        input.setRole(Role.READER);

        // Mock authentication with a non-UserDetails principal
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);

        // Act
        ResponseEntity<UserDto> response = userService.createUser(input);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testCreateUser_CurrentUSer_SUPER_Success() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto();
        input.setUsername("newUser");
        input.setEmail("newuser@example.com");
        input.setPassword("password123");
        input.setRole(Role.READER);

        User currentUser = new User("currentUser", "current@example.com", "encodedPassword", Role.SUPER);
        mockCurrentUser(currentUser); // Method to mock current authenticated user

        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(input.getUsername())).thenReturn(Optional.empty());

        User savedUser = new User(input.getUsername(), input.getEmail(), input.getPassword(), input.getRole());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        ResponseEntity<UserDto> response = userService.createUser(input);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(input.getUsername(), response.getBody().getUsername());
    }

    @Test
    public void testCreateUser_CurrentUSer_ADMIN_Success() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto();
        input.setUsername("newUser");
        input.setEmail("newuser@example.com");
        input.setPassword("password123");
        input.setRole(Role.READER);

        User currentUser = new User("currentUser", "current@example.com", "encodedPassword", Role.ADMIN);
        mockCurrentUser(currentUser); // Method to mock current authenticated user

        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(input.getUsername())).thenReturn(Optional.empty());

        User savedUser = new User(input.getUsername(), input.getEmail(), input.getPassword(), input.getRole());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        ResponseEntity<UserDto> response = userService.createUser(input);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(input.getUsername(), response.getBody().getUsername());
    }

    @Test
    public void testCreateUser_Forbidden() {
        // Arrange
        mockCurrentUser(currentUser);
        currentUser.setRole(Role.READER);

        // Act
        ResponseEntity<UserDto> response = userService.createUser(registerUserDto);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testCreateUser_UserAlreadyExists() {
        // Arrange
        mockCurrentUser(currentUser);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(currentUser));

        // Act
        ResponseEntity<UserDto> response = userService.createUser(registerUserDto);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeleteUser_CurrentUser_SUPER_Success() throws IOException {
        // Arrange
        mockCurrentUser(currentUser);
        User userToDelete = new User("username", "email@example.com", "password", Role.READER);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(userToDelete));

        // Act
        ResponseEntity<Void> response = userService.deleteUser("username", "username");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).delete(userToDelete);
    }

    @Test
    public void testDeleteUser_CurrentUser_ADMIN_Success() throws IOException {
        // Arrange
        currentUser.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);
        User userToDelete = new User("username", "email@example.com", "password", Role.READER);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(userToDelete));

        // Act
        ResponseEntity<Void> response = userService.deleteUser("username", "username");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).delete(userToDelete);
    }

    @Test
    public void testDeleteUser_Forbidden() throws IOException {
        // Arrange
        User currentUser = new User();
        currentUser.setRole(Role.READER);
        currentUser.setUsername("currentUser");
        mockCurrentUser(currentUser);

        User userToDelete = new User("username", "email@example.com", "password", Role.ADMIN);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(userToDelete));

        // Act
        ResponseEntity<Void> response = userService.deleteUser("username", "username");

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userRepository, times(0)).delete(userToDelete);
    }

    @Test
    public void testDeleteUser_NotFound() throws IOException {
        // Arrange
        mockCurrentUser(currentUser);
        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = userService.deleteUser("username", "username");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeleteUser_CurrentUserNotFound_Unauthorized() throws IOException {
        // Arrange
        String identifier = "someIdentifier";
        String type = "username";
        User currentUser = new User();
        currentUser.setUsername("currentUser");
        currentUser.setRole(Role.SUPER);
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = userService.deleteUser(identifier, type);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testUpdateUser_CurrentUser_SUPER_Success() throws IOException {
        // Arrange
        String identifier = "1";
        String type = "id";
        UserDto input = new UserDto();
        input.setUsername("updatedUser");
        input.setEmail("updated@example.com");
        input.setRole(Role.READER);

        User currentUser = new User("currentUser", "current@example.com", "encodedPassword", Role.SUPER);
        mockCurrentUser(currentUser);

        User targetUser = new User("targetUser", "target@example.com", "encodedPassword", Role.READER);
        when(userRepository.getReferenceById(anyInt())).thenReturn(targetUser);

        User updatedUser = new User(input.getUsername(), input.getEmail(), "encodedPassword", input.getRole());
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        ResponseEntity<UserDto> response = userService.updateUser(identifier, type, input);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(input.getUsername(), response.getBody().getUsername());
        assertEquals(input.getEmail(), response.getBody().getEmail());
    }

    @Test
    public void testUpdateUser_CurrentUser_ADMIN_Success() throws IOException {
        // Arrange
        String identifier = "1";
        String type = "id";
        UserDto input = new UserDto();
        input.setUsername("updatedUser");
        input.setEmail("updated@example.com");
        input.setRole(Role.READER);

        User currentUser = new User("currentUser", "current@example.com", "encodedPassword", Role.ADMIN);
        mockCurrentUser(currentUser);

        User targetUser = new User("targetUser", "target@example.com", "encodedPassword", Role.READER);
        when(userRepository.getReferenceById(anyInt())).thenReturn(targetUser);

        User updatedUser = new User(input.getUsername(), input.getEmail(), "encodedPassword", input.getRole());
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        ResponseEntity<UserDto> response = userService.updateUser(identifier, type, input);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(input.getUsername(), response.getBody().getUsername());
        assertEquals(input.getEmail(), response.getBody().getEmail());
    }

    @Test
    public void testUpdateUser_UnauthorizedWhenNoAuthentication() throws IOException {
        when(securityContext.getAuthentication()).thenReturn(null);

        ResponseEntity<UserDto> response = userService.updateUser("username", "type", new UserDto());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testUpdateUser_UnauthorizedWhenAnonymousUser() throws IOException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        ResponseEntity<UserDto> response = userService.updateUser("username", "type", new UserDto());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testUpdateUser_NotFoundWhenUserToUpdateNotFound() throws IOException {

        mockCurrentUser(new User("currentUser", "email", "1234",  Role.ADMIN));
        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());

        ResponseEntity<UserDto> response = userService.updateUser("someuser", "username", new UserDto());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testUpdateUser_ForbiddenWhenNoPermissionToUpdate() throws IOException {
        User currentUser = new User("currentUser", "email", "1234", Role.READER);
        User userToUpdate = new User("anotherUser", "email", "1234", Role.READER);
        mockCurrentUser(currentUser);
        when(userRepository.findByUsername("anotherUser")).thenReturn(Optional.of(userToUpdate));

        ResponseEntity<UserDto> response = userService.updateUser("anotherUser", "username", new UserDto());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testUpdateUser_ForbiddenWhenUpdatingAdminWithoutPermission() throws IOException {
        User currentUser = new User("currentUser", "email", "1234", Role.ADMIN);
        User adminUser = new User("adminUser", "email", "1234", Role.ADMIN);
        mockCurrentUser(currentUser);
        when(userRepository.findByUsername("adminUser")).thenReturn(Optional.of(adminUser));

        ResponseEntity<UserDto> response = userService.updateUser("adminUser", "username", new UserDto());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testUpdateUser_SuccessWhenCurrentUserIsSuper() throws IOException {
        // Arrange
        User superUser = new User("superUser", "superEmail", "1234", Role.SUPER);
        User adminUser = new User("adminUser", "adminEmail", "1234", Role.ADMIN);
        mockCurrentUser(superUser);

        when(userRepository.findByUsername("adminUser")).thenReturn(Optional.of(adminUser));

        UserDto input = new UserDto();
        input.setUsername("updatedAdminUsername");
        input.setEmail("updatedAdminEmail");
        input.setRole(Role.ADMIN);


        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(adminUser.getId());
            return savedUser;
        });

        // Act
        ResponseEntity<UserDto> response = userService.updateUser("adminUser", "username", input);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("updatedAdminUsername", response.getBody().getUsername());
        assertEquals("updatedAdminEmail", response.getBody().getEmail());
        assertEquals(Role.ADMIN, response.getBody().getRole());
    }

    @Test
    public void testUpdateUser_NoAuthentication_Unauthorized() throws IOException {
        // Arrange
        String identifier = "1";
        String type = "id";
        UserDto input = new UserDto();
        input.setUsername("updatedUser");

        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(null);

        // Act
        ResponseEntity<UserDto> response = userService.updateUser(identifier, type, input);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testUpdateUser_PrincipalNotUserDetails_Unauthorized() throws IOException {
        // Arrange
        String identifier = "1";
        String type = "id";
        UserDto input = new UserDto();
        input.setUsername("updatedUser");

        // Mock authentication with a non-UserDetails principal
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);

        // Act
        ResponseEntity<UserDto> response = userService.updateUser(identifier, type, input);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testUpdateUser_UserNotFound() throws IOException {
        // Arrange
        String identifier = "1";
        String type = "id";
        UserDto input = new UserDto();
        input.setUsername("updatedUser");

        User currentUser = new User("currentUser", "current@example.com", "encodedPassword", Role.SUPER);
        mockCurrentUser(currentUser);

        when(userRepository.getReferenceById(anyInt())).thenThrow(new EntityNotFoundException());

        // Act
        ResponseEntity<UserDto> response = userService.updateUser(identifier, type, input);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testUpdateUser_NoPermission_Forbidden() throws IOException {
        // Arrange
        String identifier = "1";
        String type = "id";
        UserDto input = new UserDto();
        input.setUsername("updatedUser");

        User currentUser = new User("currentUser", "current@example.com", "encodedPassword", Role.READER);
        mockCurrentUser(currentUser);

        User targetUser = new User("targetUser", "target@example.com", "encodedPassword", Role.ADMIN);
        when(userRepository.getReferenceById(anyInt())).thenReturn(targetUser);

        // Act
        ResponseEntity<UserDto> response = userService.updateUser(identifier, type, input);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testUpdateUser_CurrentUserNotFound_Unauthorized() throws IOException {
        // Arrange
        String identifier = "someIdentifier";
        String type = "username";
        UserDto input = new UserDto();
        input.setUsername("updatedUser");
        input.setEmail("updated@example.com");

        User currentUser = new User("currentUser", "current@example.com", "encodedPassword", Role.SUPER);
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserDto> response = userService.updateUser(identifier, type, input);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testUpdateUser_WithBooks_Success() throws IOException {
        // Arrange
        String identifier = "1";
        String type = "id";
        UserDto input = new UserDto();
        input.setUsername("updatedUser");
        input.setEmail("updated@example.com");
        input.setRole(Role.READER);

        Set<BookTitleAndAuthorDto> bookDtos = new HashSet<>();
        bookDtos.add(new BookTitleAndAuthorDto("Book1", "Author1"));
        bookDtos.add(new BookTitleAndAuthorDto("Book2", "Author2"));
        input.setBooks(bookDtos);

        User currentUser = new User("currentUser", "current@example.com", "encodedPassword", Role.SUPER);
        mockCurrentUser(currentUser);

        User targetUser = new User("targetUser", "target@example.com", "encodedPassword", Role.ADMIN);
        when(userRepository.getReferenceById(anyInt())).thenReturn(targetUser);

        User updatedUser = new User(input.getUsername(), input.getEmail(), "encodedPassword", input.getRole());
        updatedUser.setBooks(
                bookDtos.stream().map(b -> new Book(b.getTitle(), b.getAuthor())).collect(Collectors.toSet())
        );
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        ResponseEntity<UserDto> response = userService.updateUser(identifier, type, input);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(input.getUsername(), response.getBody().getUsername());
        assertEquals(input.getEmail(), response.getBody().getEmail());
        assertEquals(bookDtos.size(), response.getBody().getBooks().size());
    }

    @Test
    public void testUpdateUser_WithoutBooks_Success() throws IOException {
        // Arrange
        String identifier = "1";
        String type = "id";
        UserDto input = new UserDto();
        input.setUsername("updatedUser");
        input.setEmail("updated@example.com");
        input.setRole(Role.READER);

        input.setBooks(null);

        User currentUser = new User("currentUser", "current@example.com", "encodedPassword", Role.SUPER);
        mockCurrentUser(currentUser);

        User targetUser = new User("targetUser", "target@example.com", "encodedPassword", Role.ADMIN);
        when(userRepository.getReferenceById(anyInt())).thenReturn(targetUser);

        User updatedUser = new User(input.getUsername(), input.getEmail(), "encodedPassword", input.getRole());
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        ResponseEntity<UserDto> response = userService.updateUser(identifier, type, input);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(input.getUsername(), response.getBody().getUsername());
        assertEquals(input.getEmail(), response.getBody().getEmail());
        assertNotNull(response.getBody().getBooks());
        assertTrue(response.getBody().getBooks().isEmpty());
    }

    @Test
    public void testChangeUserPassword_Success() {
        // Arrange
        mockCurrentUser(currentUser);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");

        // Act
        ResponseEntity<Void> response = userService.changeUserPassword(currentUser.getUsername(), "newPassword");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).save(currentUser);
    }

    @Test
    public void testChangeUserPassword_NoAuthentication_Unauthorized() {
        // Arrange
        String username = "testUser";
        String newPassword = "newPassword123";

        // Simulate no authentication context
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(null);

        // Act
        ResponseEntity<Void> response = userService.changeUserPassword(username, newPassword);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testChangeUserPassword_AnonymousUser_Unauthorized() {
        // Arrange
        String username = "testUser";
        String newPassword = "newPassword123";

        // Simulate anonymous user
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);

        // Act
        ResponseEntity<Void> response = userService.changeUserPassword(username, newPassword);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testChangeUserPassword_Forbidden_WrongUser() {
        // Arrange
        String username = "differentUser"; // Username different from the current user
        String newPassword = "newPassword123";

        User currentUser = new User("testUser", "test@example.com", "encodedPassword", Role.READER);
        mockCurrentUser(currentUser);

        // Act
        ResponseEntity<Void> response = userService.changeUserPassword(username, newPassword);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testChangeUserPassword_CurrentUserNotFound_ReturnsNotFound() {
        // Arrange
        String username = "testUser";
        String newPassword = "newPassword123";

        User currentUser = new User("currentUser", "current@example.com", "encodedPassword", Role.SUPER);
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = userService.changeUserPassword(username, newPassword);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());  // The body should be null in a NOT_FOUND response
    }

    @Test
    public void testGetUserByIdentifier_Id_UserFound() {
        // Arrange
        String identifier = "1";
        String type = "id";
        User user = new User("testUser", "test@example.com", "encodedPassword", Role.READER);

        when(userRepository.getReferenceById(Integer.parseInt(identifier))).thenReturn(user);

        // Act
        Optional<User> result = userService.getUserByIdentifier(identifier, type);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    public void testGetUserByIdentifier_Id_UserNotFound() {
        // Arrange
        String identifier = "1";
        String type = "id";

        when(userRepository.getReferenceById(Integer.parseInt(identifier))).thenThrow(EntityNotFoundException.class);

        // Act
        Optional<User> result = userService.getUserByIdentifier(identifier, type);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetUserByIdentifier_InvalidId_ThrowsNumberFormatException() {
        // Arrange
        String identifier = "invalidId";
        String type = "id";

        // Act
        Optional<User> result = userService.getUserByIdentifier(identifier, type);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetUserByIdentifier_Username_UserFound() {
        // Arrange
        String identifier = "testUser";
        String type = "username";
        User user = new User("testUser", "test@example.com", "encodedPassword", Role.READER);

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.getUserByIdentifier(identifier, type);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    public void testGetUserByIdentifier_Username_UserNotFound() {
        // Arrange
        String identifier = "nonexistentUser";
        String type = "username";

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserByIdentifier(identifier, type);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetUserByIdentifier_Email_UserFound() {
        // Arrange
        String identifier = "test@example.com";
        String type = "email";
        User user = new User("testUser", "test@example.com", "encodedPassword", Role.READER);

        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.getUserByIdentifier(identifier, type);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    public void testGetUserByIdentifier_Email_UserNotFound() {
        // Arrange
        String identifier = "nonexistent@example.com";
        String type = "email";

        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserByIdentifier(identifier, type);

        // Assert
        assertFalse(result.isPresent());
    }




    @Test
    public void testGetCurrentUser_Success() {
        // Arrange
        mockCurrentUser(currentUser);

        // Act
        Optional<User> response = userService.getCurrentUser();

        // Assert
        assertTrue(response.isPresent());
        assertEquals(currentUser.getUsername(), response.get().getUsername());
    }

    @Test
    public void testGetCurrentUser_NotFound() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // Act
        Optional<User> response = userService.getCurrentUser();

        // Assert
        assertFalse(response.isPresent());
    }
}
