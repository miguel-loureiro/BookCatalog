package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.exceptions.UserNotFoundException;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import com.bookcatalog.bookcatalog.service.strategy.StrategyFactory;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateStrategy;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DeleteStrategy<User> deleteStrategy;

    @Mock
    private UpdateStrategy<User> updateStrategy;

    @Mock
    private StrategyFactory<User> strategyFactory;

    @Mock
    private User userToDelete;

    @Mock
    private User userToUpdate;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserService userService;

    private User currentUser;
    private UserDto inputDto;
    private User currentUserSuper;
    private User currentUserAdmin;
    private RegisterUserDto registerUserDtoAdmin;
    private RegisterUserDto registerUserDtoReader;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        mockSecurityContext();
        initCurrentUser();
        userService = new UserService(userRepository, passwordEncoder, strategyFactory);
        initInputDto();

        // Setup mock users
        currentUser = new User();
        currentUser.setUsername("currentUser");
        currentUser.setRole(Role.ADMIN);

        userToDelete = new User();
        userToDelete.setUsername("userToDelete");
        userToDelete.setRole(Role.READER);

        // Initialize test data
        currentUserSuper = new User("superUser", "super@example.com", "encodedPassword", Role.SUPER);
        currentUserAdmin = new User("adminUser", "admin@example.com", "encodedPassword", Role.ADMIN);

        registerUserDtoAdmin = new RegisterUserDto("newAdmin", "admin2@example.com", "password", Role.ADMIN);
        registerUserDtoReader = new RegisterUserDto("newReader", "reader@example.com", "password", Role.READER);
    }

    private void mockSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    private void initCurrentUser() {
        currentUser = new User("currentUser", "currentUser@example.com", "password", Role.ADMIN);
        when(userDetails.getUsername()).thenReturn("adminUser");
        when(userRepository.findByUsername("adminUser")).thenReturn(Optional.of(currentUser));
    }

    private void initInputDto() {
        inputDto = new UserDto();
        inputDto.setUsername("newUser");
        inputDto.setEmail("newUser@example.com");
        inputDto.setRole(Role.READER);
    }

    private void mockCurrentUser(User user) {
        if (user == null) {
            SecurityContextHolder.clearContext();
        } else {
            when(userDetails.getUsername()).thenReturn(user.getUsername());
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        }
    }

    @Test
    void testGetCurrentUser_userFound() {
        // Arrange
        String username = "testUser";
        User expectedUser = new User();
        mockCurrentUser(currentUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        // Act
        Optional<User> actualUserOpt = userService.getCurrentUser();
        if (actualUserOpt.isEmpty()) {
          fail();
        }

        User actualUser = actualUserOpt.get();
        // Assert
        assertEquals(expectedUser, actualUser);
    }

    @Test
    void testGetCurrentUser_userNotFoundWithUsername() {
        // Arrange
        String username = "testUser";
        User currentUser = new User();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        Optional<User> actualUserOpt = userService.getCurrentUser();

        // Assert
        assertTrue(actualUserOpt.isEmpty());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void getCurrentUser_PrincipalIsNotUserDetails_Return_Null() {
        // Arrange
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn("aaaa");
        // Act
        Optional<User> currentUSerInChargeOpt = userService.getCurrentUser();

        // Assert
        assertTrue(currentUSerInChargeOpt.isEmpty());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void getUserByIdentifier_Id_Success() {
        // Arrange
        String type = "id";
        String identifier = "1";
        User user = new User();  // Assuming User has a default constructor

        when(userRepository.getReferenceById(Integer.parseInt(identifier))).thenReturn(user);

        // Act
        Optional<User> resultOpt = userService.getUserByIdentifier(identifier, type);
        User result = resultOpt.get();
        // Assert
        assertEquals(user, result);
        verify(userRepository, times(1)).getReferenceById(Integer.parseInt(identifier));
    }

    @Test
    void getUserByIdentifier_Id_UserNotFound() {
        // Arrange
        String type = "id";
        String identifier = "1";

        when(userRepository.getReferenceById(Integer.parseInt(identifier)))
                .thenThrow(new EntityNotFoundException());

        // Act
        Optional<User> result = userService.getUserByIdentifier(identifier, type);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).getReferenceById(Integer.parseInt(identifier));
    }

    @Test
    void getUserByIdentifier_Id_InvalidFormat() {
        // Arrange
        String type = "id";
        String identifier = "invalid";

        // Act
        Optional<User> result = userService.getUserByIdentifier(identifier, type);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository, times(0)).getReferenceById(anyInt());  // Should not call repository
    }

    @Test
    void getUserByIdentifier_Username_Success() {
        // Arrange
        String type = "username";
        String identifier = "testUser";
        User user = new User();

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(user));

        // Act
        Optional<User> resultOpt = userService.getUserByIdentifier(identifier, type);
        User result = resultOpt.get();
        // Assert
        assertEquals(user, result);
        verify(userRepository, times(1)).findByUsername(identifier);
    }

    @Test
    void getUserByIdentifier_Username_NotFound() {
        // Arrange
        String type = "username";
        String identifier = "nonexistentUser";

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserByIdentifier(identifier, type);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByUsername(identifier);
    }

    @Test
    void getUserByIdentifier_Email_Success() {
        // Arrange
        String type = "email";
        String identifier = "user@example.com";
        User user = new User();

        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(user));

        // Act
        Optional<User> resultOpt = userService.getUserByIdentifier(identifier, type);
        User result = resultOpt.get();

        // Assert
        assertEquals(user, result);
        verify(userRepository, times(1)).findByEmail(identifier);
    }

    @Test
    void getUserByIdentifier_Email_NotFound() {
        // Arrange
        String type = "email";
        String identifier = "nonexistent@example.com";

        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserByIdentifier(identifier, type);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByEmail(identifier);
    }

    @Test
    public void testGetUserByIdentifier_InvalidType() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.getUserByIdentifier("testUser", "invalidType")
        );

        assertEquals("Invalid identifier type: invalidType", exception.getMessage());
        verify(userRepository, times(0)).getReferenceById(anyInt());
        verify(userRepository, times(0)).findByUsername(anyString());
        verify(userRepository, times(0)).findByEmail(anyString());
    }

    @Test
    void getAllUsers_ShouldReturnUserDtos() {
        // Arrange
        User user1 = new User("user1", "user1@example.com", "password1", Role.READER);
        User user2 = new User("user2", "user2@example.com", "password2", Role.READER);
        User user3 = new User("user3", "user3@example.com", "password3", Role.READER);

        List<User> usersList = List.of(user1, user2, user3);
        Sort sort = Sort.by("username");
        Pageable paging = PageRequest.of(0, usersList.size(), sort.ascending());
        Page<User> mockUserPage = new PageImpl<>(usersList, paging, usersList.size());

        when(userRepository.findAll(paging)).thenReturn(mockUserPage);

        // Act
        ResponseEntity<Page<UserDto>> response = userService.getAllUsers(0, 3);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getTotalElements());

        UserDto firstUserDto = response.getBody().getContent().get(0);
        assertEquals("user1", firstUserDto.getUsername());
        assertEquals("user1@example.com", firstUserDto.getEmail());

        UserDto secondUserDto = response.getBody().getContent().get(1);
        assertEquals("user2", secondUserDto.getUsername());
        assertEquals("user2@example.com", secondUserDto.getEmail());

        UserDto thirdUserDto = response.getBody().getContent().get(2);
        assertEquals("user3", thirdUserDto.getUsername());
        assertEquals("user3@example.com", thirdUserDto.getEmail());
    }

    @Test
    void testCreateUser_SuperCreatesAdmin() {
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUserSuper));
        when(userRepository.findByEmail(registerUserDtoAdmin.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(registerUserDtoAdmin.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerUserDtoAdmin.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<UserDto> response = userService.createUser(registerUserDtoAdmin);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(registerUserDtoAdmin.getUsername(), response.getBody().getUsername());
    }

    @Test
    void testCreateUser_SuperCreatesReader() {
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUserSuper));
        when(userRepository.findByEmail(registerUserDtoReader.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(registerUserDtoReader.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerUserDtoReader.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<UserDto> response = userService.createUser(registerUserDtoReader);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(registerUserDtoReader.getUsername(), response.getBody().getUsername());
    }

    @Test
    void testCreateUser_AdminCreatesReader() {
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUserAdmin));
        when(userRepository.findByEmail(registerUserDtoReader.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(registerUserDtoReader.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerUserDtoReader.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<UserDto> response = userService.createUser(registerUserDtoReader);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(registerUserDtoReader.getUsername(), response.getBody().getUsername());
    }

    @Test
    void testCreateUser_AdminCreatesAdminShouldFail() {
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUserAdmin));

        ResponseEntity<UserDto> response = userService.createUser(registerUserDtoAdmin);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testCreateUser_SuperCreatingSuper_ShouldReturnForbidden() {
        // Arrange
        currentUser = new User();
        currentUser.setRole(Role.SUPER);
        RegisterUserDto input = new RegisterUserDto("newSuper", "super@example.com", "password123", Role.SUPER);

        mockCurrentUser(currentUser);

        // Act
        ResponseEntity<UserDto> response = userService.createUser(input);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testCreateUser_UserAlreadyExists() {
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUserSuper));
        when(userRepository.findByEmail(registerUserDtoReader.getEmail())).thenReturn(Optional.of(new User()));
        when(userRepository.findByUsername(registerUserDtoReader.getUsername())).thenReturn(Optional.empty());

        ResponseEntity<UserDto> response = userService.createUser(registerUserDtoReader);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userRepository, times(0)).save(any(User.class));
        assertEquals("User already in the database", response.getHeaders().getFirst("Error-Message"));
    }

    @Test
    void testCreateUser_NoCurrentUser() {
        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        ResponseEntity<UserDto> response = userService.createUser(registerUserDtoReader);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testDeleteUser_StrategyNotFound() throws IOException {
        // Arrange
        when(strategyFactory.getDeleteStrategy("id")).thenReturn(null);

        // Act
        ResponseEntity<Void> response = userService.deleteUser("4", "id");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeleteUser_CurrentUserNotFound() throws IOException {
        // Arrange
        when(strategyFactory.getDeleteStrategy("id")).thenReturn(deleteStrategy);
        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = userService.deleteUser("4", "id");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteUser_UserToDeleteNotFound() throws IOException {
        // Arrange
        String identifier = "123";
        String type = "username";

        when(strategyFactory.getDeleteStrategy(type)).thenReturn(deleteStrategy);
        when(userService.getUserByIdentifier(identifier, type)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = userService.deleteUser(identifier, type);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(deleteStrategy, never()).delete(any(User.class));
    }

    @Test
    void testDeleteUser_ForbiddenToDeleteAdmin_ByNonSuper() throws IOException {
        // Arrange
        currentUser.setRole(Role.ADMIN);
        userToDelete.setRole(Role.ADMIN);
        when(strategyFactory.getDeleteStrategy("id")).thenReturn(deleteStrategy);
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(userRepository.getReferenceById(4)).thenReturn(userToDelete);

        // Act
        ResponseEntity<Void> response = userService.deleteUser("4", "id");

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(deleteStrategy, never()).delete(any(User.class));
    }

    @Test
    void testDeleteUser_AdminDeletingSelf() throws IOException {
        // Arrange
        currentUser = new User();
        currentUser.setUsername("adminUser");
        currentUser.setRole(Role.ADMIN);

        when(strategyFactory.getDeleteStrategy("username")).thenReturn(deleteStrategy);
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(userService.getUserByIdentifier("adminUser", "username")).thenReturn(Optional.of(currentUser));

        // Act
        ResponseEntity<Void> response = userService.deleteUser("adminUser", "username");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deleteStrategy, times(1)).delete(currentUser);
    }

    @Test
    void testDeleteUser_SuperUserDeletingAdmin() throws IOException {
        // Arrange
        currentUser.setRole(Role.SUPER);
        mockCurrentUser(currentUser);
        userToDelete.setRole(Role.ADMIN);

        when(strategyFactory.getDeleteStrategy("id")).thenReturn(deleteStrategy);
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(userRepository.getReferenceById(7)).thenReturn(userToDelete);

        // Act
        ResponseEntity<Void> response = userService.deleteUser("7", "id");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deleteStrategy, times(1)).delete(userToDelete);
    }

    @Test
    void testDeleteUser_ForbiddenToDelete_ByPermissionCheck() throws IOException {
        // Arrange
        User currentUser = new User();
        currentUser.setRole(Role.READER);
        currentUser.setUsername("testuser");
        mockCurrentUser(currentUser);

        userToDelete.setRole(Role.GUEST);

        when(strategyFactory.getDeleteStrategy("id")).thenReturn(deleteStrategy);
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(userRepository.getReferenceById(4)).thenReturn(userToDelete);

        // Act
        ResponseEntity<Void> response = userService.deleteUser("4", "id");

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(deleteStrategy, never()).delete(any(User.class));
    }

    @Test
    void testDeleteUser_Success() throws IOException {
        // Arrange
        when(strategyFactory.getDeleteStrategy("id")).thenReturn(deleteStrategy);
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(userRepository.getReferenceById(4)).thenReturn(userToDelete);

        // Act
        ResponseEntity<Void> response = userService.deleteUser("4", "id");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deleteStrategy, times(1)).delete(userToDelete);
    }

    @Test
    void testDeleteUser_IllegalArgumentExceptionCaught() throws IOException {
        // Arrange
        when(strategyFactory.getDeleteStrategy("id")).thenReturn(deleteStrategy);
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(userRepository.getReferenceById(4)).thenReturn(userToDelete);

        doThrow(new IllegalArgumentException("Invalid argument")).when(deleteStrategy).delete(userToDelete);

        // Act
        ResponseEntity<Void> response = userService.deleteUser("4", "id");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeleteUser_IOExceptionCaught() throws IOException {
        // Arrange
        when(strategyFactory.getDeleteStrategy("id")).thenReturn(deleteStrategy);
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(userRepository.getReferenceById(4)).thenReturn(userToDelete);

        doThrow(new IOException("IO error")).when(deleteStrategy).delete(userToDelete);

        // Act
        ResponseEntity<Void> response = userService.deleteUser("4", "id");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testUpdateUser_Success() throws IOException {
        // Arrange
        UserDto newDetails = new UserDto("newUsername", "newEmail", Role.GUEST);
        User newUserDetails = new User(newDetails);
        User currentUser = new User();
        currentUser.setUsername("currentUser");
        currentUser.setRole(Role.ADMIN);

        User userToBeUpdated = new User();
        userToBeUpdated.setEmail("updateuser@email.com");
        userToBeUpdated.setRole(Role.READER);

        mockCurrentUser(currentUser);
        when(strategyFactory.getUpdateStrategy("email")).thenReturn(updateStrategy);
        when(userRepository.findByEmail("updateuser@email.com")).thenReturn(Optional.of(userToBeUpdated));

        // Act
        ResponseEntity<Void> response = userService.updateUser("updateuser@email.com", "email", newDetails);

        // Assert
        verify(updateStrategy).update(userToBeUpdated, newUserDetails, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateUser_IdentifierIsEmail_Success_SameUser() throws IOException {
        // Arrange
        currentUser = new User();
        currentUser.setUsername("user1");
        currentUser.setRole(Role.READER);

        inputDto = new UserDto();
        inputDto.setUsername("user1");
        mockCurrentUser(currentUser);

        when(strategyFactory.getUpdateStrategy("id")).thenReturn(updateStrategy);
        when(userRepository.getReferenceById(4)).thenReturn(currentUser);

        // Act
        ResponseEntity<Void> response = userService.updateUser("4", "id", inputDto);

        // Assert
        verify(updateStrategy, times(1)).update(eq(currentUser), any(User.class), eq(null));
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateUser_Success_HigherRank() throws IOException {
        // Arrange
        UserDto newDetails = new UserDto("newUsername", "newEmail", Role.GUEST);
        User newUserDetails = new User(newDetails);
        User currentUser = new User();
        currentUser.setUsername("currentUser");
        currentUser.setRole(Role.SUPER);

        User userToBeUpdated = new User();
        userToBeUpdated.setEmail("updateuser@email.com");
        userToBeUpdated.setRole(Role.READER);

        mockCurrentUser(currentUser);
        when(strategyFactory.getUpdateStrategy("email")).thenReturn(updateStrategy);
        when(userRepository.findByEmail("updateuser@email.com")).thenReturn(Optional.of(userToBeUpdated));

        // Act
        ResponseEntity<Void> response = userService.updateUser("updateuser@email.com", "email", newDetails);

        // Assert
        verify(updateStrategy).update(userToBeUpdated, newUserDetails, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testUpdateUser_NoStrategyType() throws IOException {
        // Act
        UserDto input = new UserDto();
        ResponseEntity<Void> response = userService.updateUser("testIdentifier", "INVALID_TYPE", input);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateUser_NoPermission() throws IOException {
        // Arrange
        currentUser.setUsername("user1");
        currentUser.setRole(Role.READER);

        User userToBeUpdated = new User();
        userToBeUpdated.setEmail("updateuser@email.com");
        userToBeUpdated.setRole(Role.ADMIN);

        UserDto input = new UserDto();

        mockCurrentUser(currentUser);

        when(strategyFactory.getUpdateStrategy("email")).thenReturn(updateStrategy);
        when(userService.getUserByIdentifier("updateuser@email.com", "email")).thenReturn(Optional.of(userToUpdate));

        // Act
        ResponseEntity<Void> response = userService.updateUser("updateuser@email.com", "email", input);

        // Assert
        verify(updateStrategy, never()).update(userToBeUpdated, new User(input), null);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testUpdateUser_IllegalArgument() throws IOException {
        // Arrange
        mockCurrentUser(currentUser);

        when(strategyFactory.getUpdateStrategy("username")).thenReturn(updateStrategy);
        when(userRepository.getReferenceById(1)).thenThrow(new IllegalArgumentException("Invalid identifier"));

        // Act
        ResponseEntity<Void> response = userService.updateUser("1", "id", new UserDto());

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateUser_SuperUpdatingAdmin_ShouldReturnOk() throws IOException {
        // Arrange
        UserDto newDetails = new UserDto("newUsername", "newEmail", Role.GUEST);
        User superUser = new User("superUser", "super@example.com", "encodedPassword", Role.SUPER);
        when(userService.getCurrentUser()).thenReturn(Optional.of(superUser));

        User adminUser = new User("adminUser", "admin@example.com", "encodedPassword", Role.ADMIN);

        when(userService.getUserByIdentifier("adminUser", "username")).thenReturn(Optional.of(adminUser));
        when(strategyFactory.getUpdateStrategy("username")).thenReturn(updateStrategy);

        // Act
        ResponseEntity<Void> response = userService.updateUser("adminUser", "username", newDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(updateStrategy, times(1)).update(eq(adminUser), any(User.class), isNull());
    }

    @Test
    void testUpdateUser_AdminUpdatingSelf_ShouldReturnOk() throws IOException {
        // Arrange
        User adminUser = new User("adminUser", "admin@example.com", "encodedPassword", Role.ADMIN);

        when(userService.getCurrentUser()).thenReturn(Optional.of(adminUser));
        when(userService.getUserByIdentifier("adminUser", "username")).thenReturn(Optional.of(adminUser));
        when(strategyFactory.getUpdateStrategy("username")).thenReturn(updateStrategy);

        UserDto newDetails = new UserDto("newUsername", "newEmail", Role.ADMIN);

        // Act
        ResponseEntity<Void> response = userService.updateUser("adminUser", "username", newDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(updateStrategy, times(1)).update(eq(adminUser), any(User.class), isNull());
    }

    @Test
    void testUpdateUser_AdminUpdatingAnotherAdmin_ShouldReturnForbidden() throws IOException {
        // Arrange
        User adminUser = new User();
        adminUser.setUsername("adminUser");
        adminUser.setRole(Role.ADMIN);

        User anotherAdminUser = new User();
        anotherAdminUser.setUsername("anotherAdminUser");
        anotherAdminUser.setRole(Role.ADMIN);
        when(strategyFactory.getUpdateStrategy("username")).thenReturn(updateStrategy);
        when(userService.getCurrentUser()).thenReturn(Optional.of(adminUser));
        when(userService.getUserByIdentifier("anotherAdminUser", "username")).thenReturn(Optional.of(anotherAdminUser));

        ResponseEntity<Void> response = userService.updateUser("anotherAdminUser", "username", new UserDto());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(updateStrategy, never()).update(any(User.class), any(User.class), any());
    }

    @Test
    void testUpdateUser_ReaderUpdatingAdmin_ShouldReturnForbidden() throws IOException {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("currentUser");
        currentUser.setRole(Role.READER);
        mockCurrentUser(currentUser);

        User adminUser = new User();
        adminUser.setUsername("adminUser");
        adminUser.setRole(Role.ADMIN);

        when(strategyFactory.getUpdateStrategy("username")).thenReturn(updateStrategy);
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(userService.getUserByIdentifier("adminUser", "username")).thenReturn(Optional.of(adminUser));

        // Act
        ResponseEntity<Void> response = userService.updateUser("adminUser", "username", new UserDto());

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(updateStrategy, never()).update(any(User.class), any(User.class), any());
    }

    @Test
    void updateUser_ShouldReturnBadRequest_WhenIllegalArgumentExceptionIsThrown() throws IOException {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid argument")).when(updateStrategy).update(any(User.class), any(User.class), any());

        // Act
        ResponseEntity<Void> response = userService.updateUser("identifier", "type", inputDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateUser_UserToUpdateNotFound_ShouldReturnNotFound() throws IOException {
        // Arrange
        String identifier = "userToUpdate";
        String type = "username";
        UserDto newDetails = new UserDto("newUsername", "newEmail", Role.READER);

        User currentUser = new User("currentUsername", "current@example.com", "encodedPassword", Role.ADMIN);
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(userService.getUserByIdentifier(identifier, type)).thenReturn(Optional.empty());
        when(strategyFactory.getUpdateStrategy(type)).thenReturn(updateStrategy);

        // Act
        ResponseEntity<Void> response = userService.updateUser(identifier, type, newDetails);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateUser_UserNotFoundException_ShouldReturnNotFound() throws IOException {
        // Arrange
        UserDto newDetails = new UserDto("newUsername", "newEmail", Role.READER);
        User currentUser = new User("superUser", "super@example.com", "encodedPassword", Role.SUPER);
        User userToUpdate = new User("userToUpdate", "update@example.com", "encodedPassword", Role.READER);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(userService.getUserByIdentifier("userToUpdate", "username")).thenReturn(Optional.of(userToUpdate));
        when(strategyFactory.getUpdateStrategy("username")).thenReturn(updateStrategy);
        doThrow(new UserNotFoundException("User not found", null)).when(updateStrategy).update(eq(userToUpdate), any(User.class), isNull());

        // Act
        ResponseEntity<Void> response = userService.updateUser("userToUpdate", "username", newDetails);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateUser_CurrentUserNotFoundException_ShouldReturnNotFound() throws IOException {
        // Arrange
        UserDto newDetails = new UserDto("newUsername", "newEmail", Role.READER);
        User currentUser = new User("superUser", "super@example.com", "encodedPassword", Role.ADMIN);
        mockCurrentUser(currentUser);
        User userToUpdate = new User("userToUpdate", "update@example.com", "encodedPassword", Role.READER);

        when(userService.getCurrentUser()).thenReturn(Optional.empty());
        when(strategyFactory.getUpdateStrategy("username")).thenReturn(updateStrategy);

        // Act
        ResponseEntity<Void> response = userService.updateUser("userToUpdate", "username", newDetails);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateUser_IllegalArgumentException_ShouldReturnBadRequest() throws IOException {
        // Arrange
        UserDto newDetails = new UserDto("newUsername", "newEmail", Role.READER);
        User currentUser = new User("superUser", "super@example.com", "encodedPassword", Role.SUPER);
        User userToUpdate = new User("userToUpdate", "update@example.com", "encodedPassword", Role.READER);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(userService.getUserByIdentifier("userToUpdate", "username")).thenReturn(Optional.of(userToUpdate));
        when(strategyFactory.getUpdateStrategy("username")).thenReturn(updateStrategy);
        doThrow(new IllegalArgumentException("Invalid input")).when(updateStrategy).update(eq(userToUpdate), any(User.class), isNull());

        // Act
        ResponseEntity<Void> response = userService.updateUser("userToUpdate", "username", newDetails);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateUser_IOException_ShouldReturnInternalServerError() throws IOException {
        // Arrange
        UserDto newDetails = new UserDto("newUsername", "newEmail", Role.READER);
        User currentUser = new User("superUser", "super@example.com", "encodedPassword", Role.SUPER);
        User userToUpdate = new User("userToUpdate", "update@example.com", "encodedPassword", Role.READER);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(userService.getUserByIdentifier("userToUpdate", "username")).thenReturn(Optional.of(userToUpdate));
        when(strategyFactory.getUpdateStrategy("username")).thenReturn(updateStrategy);
        doThrow(new IOException("I/O error occurred")).when(updateStrategy).update(eq(userToUpdate), any(User.class), isNull());

        // Act
        ResponseEntity<Void> response = userService.updateUser("userToUpdate", "username", newDetails);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testChangeUserPassword_Success() {
        // Arrange
        String username = "currentUser";
        String newPassword = "newSecurePassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(currentUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");

        // Act
        ResponseEntity<Void> response = userService.changeUserPassword(username, newPassword);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(currentUser);
        assertEquals("encodedPassword", currentUser.getPassword());
    }

    @Test
    void testChangeUserPassword_DifferentUser_Forbidden() {
        // Arrange
        String username = "differentUser";
        String newPassword = "newSecurePassword";

        // Act
        ResponseEntity<Void> response = userService.changeUserPassword(username, newPassword);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testChangeUserPassword_UserNotFound() {
        // Arrange
        currentUser.setUsername("currentUser");
        mockCurrentUser(currentUser);
        String newPassword = "newPassword";

        when(userRepository.findByUsername("currentUser")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = userService.changeUserPassword("otherUser", newPassword);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

/*
    @Test
    public void testHasPermissionToDeleteUser_SameSuperUser_ShouldReturnFalse() {
        // Arrange
        User currentUser = new User("superUser", "super@domain.com", "password", Role.SUPER);
        User targetUser = new User("superUser", "super@domain.com", "password", Role.SUPER);

        // Act
        boolean result = userService.hasPermissionToDeleteUser(currentUser, targetUser);

        // Assert
        assertFalse(result, "Super user should not be able to delete themselves");
    }

    @Test
    public void testHasPermissionToDeleteUser_AdminDeletingAnotherAdmin_ShouldReturnFalse() {
        // Arrange
        User currentUser = new User("adminUser1", "admin1@domain.com", "password", Role.ADMIN);
        User targetUser = new User("adminUser2", "admin2@domain.com", "password", Role.ADMIN);

        // Act
        boolean result = userService.hasPermissionToDeleteUser(currentUser, targetUser);

        // Assert
        assertFalse(result, "Admin user should not be able to delete another admin user");
    }

    @Test
    public void testHasPermissionToDeleteUser_ReaderDeletingAdmin_ShouldReturnFalse() {
        // Arrange
        User currentUser = new User("readerUser", "reader@domain.com", "password", Role.READER);
        User targetUser = new User("adminUser", "admin@domain.com", "password", Role.ADMIN);

        // Act
        boolean result = userService.hasPermissionToDeleteUser(currentUser, targetUser);

        // Assert
        assertFalse(result, "Reader user should not be able to delete an admin user");
    }

    // Unit tests for hasPermissionToUpdateUser

    @Test
    public void testHasPermissionToUpdateUser_SameSuperUser_ShouldReturnFalse() {
        // Arrange
        User currentUser = new User("superUser", "super@domain.com", "password", Role.SUPER);
        User targetUser = new User("superUser", "super@domain.com", "password", Role.SUPER);

        // Act
        boolean result = userService.hasPermissionToUpdateUser(currentUser, targetUser);

        // Assert
        assertFalse(result, "Super user should not be able to update themselves");
    }

    @Test
    public void testHasPermissionToUpdateUser_AdminUpdatingAnotherAdmin_ShouldReturnFalse() {
        // Arrange
        User currentUser = new User("adminUser1", "admin1@domain.com", "password", Role.ADMIN);
        User targetUser = new User("adminUser2", "admin2@domain.com", "password", Role.ADMIN);

        // Act
        boolean result = userService.hasPermissionToUpdateUser(currentUser, targetUser);

        // Assert
        assertFalse(result, "Admin user should not be able to update another admin user");
    }

    @Test
    public void testHasPermissionToUpdateUser_ReaderUpdatingAdmin_ShouldReturnFalse() {
        // Arrange
        User currentUser = new User("readerUser", "reader@domain.com", "password", Role.READER);
        User targetUser = new User("adminUser", "admin@domain.com", "password", Role.ADMIN);

        // Act
        boolean result = userService.hasPermissionToUpdateUser(currentUser, targetUser);

        // Assert
        assertFalse(result, "Reader user should not be able to update an admin user");
    }
 */
}
