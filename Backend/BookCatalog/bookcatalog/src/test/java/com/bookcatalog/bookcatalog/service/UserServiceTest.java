package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.exceptions.UserNotFoundException;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails mockUserDetails;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RegisterUserDto mockRegisterUserDto;

    @Mock
    private UserDto mockCurrentUser;

    @Mock
    private Page<User> mockUserPage;

    @Mock
    private Page<UserDto> mockUserDtoPage;

    @Mock
    private DeleteStrategy<User> deleteStrategy;

    @Mock
    private UpdateStrategy<User> updateStrategy;

    @Mock
    private Map<String, UpdateStrategy<User>> updateStrategiesMap;

    @Mock
    private Map<String, DeleteStrategy<User>> deleteStrategiesMap;

    @Mock
    private User savedUser;

    @Mock
    private User userToDelete;

    @Mock
    private User adminToDelete;

    @Mock
    private User userToUpdate;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserService userService;

    private User currentUser;
    private UserDto inputDto;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        currentUser = new User("currentUser", "currentUser@example.com", "password", Role.ADMIN);
        adminToDelete = new User("adminToDelete", "adminToDelete@example.com", "password", Role.ADMIN);
        when(authentication.getPrincipal()).thenReturn(currentUser);

        deleteStrategiesMap = new HashMap<>();
        deleteStrategiesMap.put("username", deleteStrategy);
        updateStrategiesMap = new HashMap<>();
        updateStrategiesMap.put("username", updateStrategy);
        userService = new UserService(userRepository, passwordEncoder, updateStrategiesMap, deleteStrategiesMap);

        inputDto = new UserDto();
        inputDto.setUsername("newUser");
        inputDto.setEmail("newUser@example.com");
        inputDto.setRole(Role.READER);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("adminUser");

        when(userRepository.findByUsername("adminUser")).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsername("userToDelete")).thenReturn(Optional.of(userToDelete));
        when(userRepository.findByUsername("adminToDelete")).thenReturn(Optional.of(adminToDelete));

        deleteStrategiesMap = new HashMap<>();
        deleteStrategiesMap.put("id", deleteStrategy);
        deleteStrategiesMap.put("username", deleteStrategy);
        deleteStrategiesMap.put("email", deleteStrategy);

        updateStrategiesMap = new HashMap<>();
        updateStrategiesMap.put("id", updateStrategy);
        updateStrategiesMap.put("username", updateStrategy);
        updateStrategiesMap.put("email", updateStrategy);

        userService = new UserService(userRepository, passwordEncoder, updateStrategiesMap, deleteStrategiesMap);

        currentUser = new User();
        userToDelete = new User();
        adminToDelete = new User();
        userToUpdate = new User();

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
    void testGetCurrentUser_userFound() {
        // Arrange
        String username = "testUser";
        User expectedUser = new User();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        // Act
        SecurityContextHolder.setContext(securityContext);
        User actualUser = userService.getCurrentUser();

        // Assert
        assertEquals(expectedUser, actualUser);
    }

    @Test
    void testGetCurrentUser_userNotFoundWithUsername() {
        // Arrange
        String username = "testUser";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        User currentUser = userService.getCurrentUser();

        // Assert
        assertNull(currentUser);
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void getCurrentUser_PrincipalIsNotUserDetails_Return_Null() {
        // Arrange
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn("aaaa");
        // Act
        User currentUSerInCharge = userService.getCurrentUser();

        // Assert
        assertNull(currentUSerInCharge);
        verify(userRepository, never()).findByUsername(anyString());
    }

    // --------------------------------
    @Test
    void getUserByIdentifier_Id_Success() {
        // Arrange
        String type = "id";
        String identifier = "1";
        User user = new User();  // Assuming User has a default constructor

        when(userRepository.getReferenceById(Integer.parseInt(identifier))).thenReturn(user);

        // Act
        User result = userService.getUserByIdentifier(identifier, type);

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

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUserByIdentifier(identifier, type));
        verify(userRepository, times(1)).getReferenceById(Integer.parseInt(identifier));
    }

    @Test
    void getUserByIdentifier_Id_InvalidFormat() {
        // Arrange
        String type = "id";
        String identifier = "invalid";

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUserByIdentifier(identifier, type));
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
        User result = userService.getUserByIdentifier(identifier, type);

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

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUserByIdentifier(identifier, type));
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
        User result = userService.getUserByIdentifier(identifier, type);

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

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUserByIdentifier(identifier, type));
        verify(userRepository, times(1)).findByEmail(identifier);
    }





    // --------------------------------

    @Test
    public void testGetUserByIdentifier_InvalidType() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.getUserByIdentifier("testUser", "invalidType")
        );

        assertEquals("Invalid identifier type: invalidType", exception.getMessage());
        verify(userRepository, times(0)).getReferenceById(anyInt());  // Should not call repository
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
    void createUserNonAdmin_ValidInput_ReturnsUserDto() {

        // Arrange
        when(mockRegisterUserDto.getUsername()).thenReturn("guest");
        when(mockRegisterUserDto.getEmail()).thenReturn("guest@example.com");
        when(mockRegisterUserDto.getPassword()).thenReturn("securePassword");

        when(passwordEncoder.encode("securePassword")).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(savedUser.getUsername()).thenReturn("guest");
        when(savedUser.getEmail()).thenReturn("guest@example.com");
        when(savedUser.getRole()).thenReturn(Role.GUEST);

        UserDto result = userService.createUserNonAdmin(mockRegisterUserDto);

        // Assert
        assertNotNull(result);
        assertEquals("guest", result.getUsername());
        assertEquals("guest@example.com", result.getEmail());
        assertEquals(Role.GUEST, result.getRole());
        verify(mockRegisterUserDto).getUsername();
        verify(mockRegisterUserDto).getEmail();
        verify(mockRegisterUserDto).getPassword();
        verify(passwordEncoder).encode("securePassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createAdministrator_ValidInput_ReturnsUserDto() {

        // Arrange
        when(mockRegisterUserDto.getUsername()).thenReturn("admin");
        when(mockRegisterUserDto.getEmail()).thenReturn("admin@example.com");
        when(mockRegisterUserDto.getPassword()).thenReturn("securePassword");

        when(passwordEncoder.encode("securePassword")).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(savedUser.getUsername()).thenReturn("admin");
        when(savedUser.getEmail()).thenReturn("admin@example.com");
        when(savedUser.getRole()).thenReturn(Role.ADMIN);

        UserDto result = userService.createAdministrator(mockRegisterUserDto);

        // Assert
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("admin@example.com", result.getEmail());
        assertEquals(Role.ADMIN, result.getRole());
        verify(mockRegisterUserDto).getUsername();
        verify(mockRegisterUserDto).getEmail();
        verify(mockRegisterUserDto).getPassword();
        verify(passwordEncoder).encode("securePassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testDeleteUser_IdentifierIsId_success_sameUser() throws IOException {
        // Arrange
        currentUser.setUsername("sameUser");
        currentUser.setRole(Role.ADMIN);
        userToDelete.setUsername("sameUser");
        userToDelete.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);

        when(userService.getUserByIdentifier("1", "id")).thenReturn(userToDelete);

        // Act
        ResponseEntity<Void> response = userService.deleteUser("1", "id");

        // Assert
        verify(deleteStrategy).delete(userToDelete);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteUser_IdentifierIsUsername_Success_SameUser() throws IOException {
        // Arrange
        currentUser.setUsername("sameUser");
        currentUser.setRole(Role.ADMIN);
        userToDelete.setUsername("sameUser");
        userToDelete.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);

        when(userRepository.findByUsername("sameUser")).thenReturn(Optional.of(userToDelete));

        // Act
        ResponseEntity<Void> response = userService.deleteUser("sameUser", "username");

        // Assert
        verify(deleteStrategy).delete(userToDelete);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteUser_IdentifierIsEmail_Success_SameUser() throws IOException {
        // Arrange
        currentUser.setUsername("sameUser");
        currentUser.setEmail("sameUser@email.com");
        currentUser.setRole(Role.ADMIN);

        userToDelete.setUsername("sameUser");
        userToDelete.setEmail("sameUser@email.com");
        userToDelete.setRole(Role.ADMIN);


        mockCurrentUser(currentUser);

        when(userRepository.findByEmail("sameUser@email.com")).thenReturn(Optional.of(userToDelete));

        // Act
        ResponseEntity<Void> response = userService.deleteUser("sameUser@email.com", "email");

        // Assert
        verify(deleteStrategy).delete(userToDelete);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteUser_Success_HigherRank() throws IOException {
        // Arrange
        currentUser.setUsername("admin");
        currentUser.setRole(Role.ADMIN);

        userToDelete.setUsername("user");
        userToDelete.setRole(Role.READER);

        mockCurrentUser(currentUser);

        when(userService.getUserByIdentifier("1", "id")).thenReturn(userToDelete);

        // Act
        ResponseEntity<Void> response = userService.deleteUser("1", "id");

        // Assert
        verify(deleteStrategy).delete(userToDelete);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteUser_NoPermission() throws IOException {
        // Arrange
        currentUser.setUsername("user1");
        currentUser.setRole(Role.READER);

        userToDelete.setUsername("user2");
        userToDelete.setRole(Role.ADMIN);

        mockCurrentUser(currentUser);

        when(userService.getUserByIdentifier("1", "id")).thenReturn(userToDelete);

        // Act
        ResponseEntity<Void> response = userService.deleteUser("1", "id");

        // Assert
        verify(deleteStrategy, never()).delete(userToDelete);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testDeleteUser_StrategyNotFound() throws IOException {
        // Act
        ResponseEntity<Void> response = userService.deleteUser("testIdentifier", "INVALID_TYPE");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeleteUser_UserNotFound() throws IOException {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("currentUser");
        currentUser.setRole(Role.ADMIN);

        mockCurrentUser(currentUser);

        when(userRepository.findByUsername("sameUser")).thenThrow(new EntityNotFoundException("User not found", null));

        // Act
        ResponseEntity<Void> response = userService.deleteUser("nonExistingUser", "username");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteUser_IllegalArgument() throws IOException {
        // Arrange
        mockCurrentUser(currentUser);
        when(userService.getUserByIdentifier("1", "id")).thenThrow(new IllegalArgumentException("Invalid identifier"));

        // Act
        ResponseEntity<Void> response = userService.deleteUser("1", "id");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeleteUser_IOException() throws IOException {
        // Arrange
        currentUser.setUsername("sameUser");
        currentUser.setRole(Role.ADMIN);
        userToDelete.setUsername("sameUser");
        userToDelete.setRole(Role.GUEST);
        mockCurrentUser(currentUser);

        when(userService.getUserByIdentifier("5", "id")).thenReturn(userToDelete);
        doThrow(new IOException("IO Exception")).when(deleteStrategy).delete(userToDelete);

        // Act
        ResponseEntity<Void> response = userService.deleteUser("5", "id");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testDeleteAdministrator_IdentifierIsUsername_success_sameUser() throws IOException {
        // Arrange
        currentUser.setUsername("sameUser");
        currentUser.setRole(Role.ADMIN);
        adminToDelete.setUsername("sameUser");
        adminToDelete.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);
        when(userRepository.findByUsername("adminToDelete")).thenReturn(Optional.of(adminToDelete));

        // Act
        ResponseEntity<Void> response = userService.deleteAdministrator("adminToDelete", "username");

        // Assert
        verify(deleteStrategy).delete(adminToDelete);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteAdministrator_success_higherRank() throws IOException {
        // Arrange
        currentUser.setUsername("admin");
        currentUser.setRole(Role.SUPER);

        adminToDelete.setUsername("user");
        adminToDelete.setRole(Role.ADMIN);

        mockCurrentUser(currentUser);

        when(userService.getUserByIdentifier("1", "id")).thenReturn(adminToDelete);

        // Act
        ResponseEntity<Void> response = userService.deleteAdministrator("1", "id");

        // Assert
        verify(deleteStrategy).delete(adminToDelete);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteAdministrator_NoPermission() throws IOException {
        // Arrange
        currentUser.setUsername("user1");
        currentUser.setRole(Role.READER);

        adminToDelete.setUsername("user2");
        adminToDelete.setRole(Role.ADMIN);

        mockCurrentUser(currentUser);

        when(userService.getUserByIdentifier("1", "id")).thenReturn(adminToDelete);

        // Act
        ResponseEntity<Void> response = userService.deleteAdministrator("1", "id");

        // Assert
        verify(deleteStrategy, never()).delete(adminToDelete);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testDeleteAdministrator_StrategyNotFound() throws IOException {

        // Act
        ResponseEntity<Void> response = userService.deleteAdministrator("testIdentifier", "INVALID_TYPE");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeleteAdministrator_userNotFound() throws IOException {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("currentUser");
        currentUser.setRole(Role.ADMIN);

        mockCurrentUser(currentUser);

        when(userRepository.findByUsername("newAdmin")).thenThrow(new EntityNotFoundException("User not found", null));

        // Act
        ResponseEntity<Void> response = userService.deleteUser("nonExistingAdmin", "username");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteAdministrator_IllegalArgument() throws IOException {
        // Arrange
        mockCurrentUser(currentUser);
        when(userService.getUserByIdentifier("1", "id")).thenThrow(new IllegalArgumentException("Invalid identifier"));

        // Act
        ResponseEntity<Void> response = userService.deleteAdministrator("1", "id");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeleteAdministrator_IOException() throws IOException {
        // Arrange
        currentUser.setUsername("sameUser");
        currentUser.setRole(Role.ADMIN);
        adminToDelete.setUsername("sameUser");
        adminToDelete.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);

        when(userService.getUserByIdentifier("5", "id")).thenReturn(adminToDelete);
        doThrow(new IOException("IO Exception")).when(deleteStrategy).delete(adminToDelete);

        // Act
        ResponseEntity<Void> response = userService.deleteAdministrator("5", "id");

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

        userToUpdate.setRole(Role.READER);

        mockCurrentUser(currentUser);
        when(userRepository.findByEmail("updateuser@email.com")).thenReturn(Optional.of(userToUpdate));

        // Act
        ResponseEntity<Void> response = userService.updateUser("updateuser@email.com", "email", newDetails);

        // Assert
        verify(updateStrategy).update(userToUpdate, newUserDetails, null);
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
    public void testUpdateUser_UserNotFound_Throw_UserNotFoundException() throws IOException {

        // Arrange
        User currentUser = new User();
        currentUser.setUsername("currentUser");
        currentUser.setRole(Role.ADMIN);

        UserDto input = new UserDto();

        mockCurrentUser(currentUser);

        when(userRepository.findByUsername("sameUser")).thenThrow(new EntityNotFoundException("User not found", null));

        // Act
        ResponseEntity<Void> response = userService.updateUser("nonExistingUser", "username", input);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateUser_NoPermission() throws IOException {
        // Arrange
        currentUser.setUsername("user1");
        currentUser.setRole(Role.READER);

        userToUpdate.setRole(Role.ADMIN);

        UserDto input = new UserDto();

        mockCurrentUser(currentUser);

        when(userService.getUserByIdentifier("1", "id")).thenReturn(userToUpdate);

        // Act
        ResponseEntity<Void> response = userService.updateUser("1", "id", input);

        // Assert
        verify(updateStrategy, never()).update(userToUpdate, new User(input), null);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testUpdateUser_IllegalArgument() throws IOException {
        // Arrange
        mockCurrentUser(currentUser);
        when(userService.getUserByIdentifier("1", "id")).thenThrow(new IllegalArgumentException("Invalid identifier"));

        // Act
        ResponseEntity<Void> response = userService.updateUser("1", "id", new UserDto());

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateAdministrator_Success() throws IOException {

        // Arrange
        UserDto newDetails = new UserDto("newUsername", "newEmail", Role.GUEST);
        User newUserDetails = new User(newDetails);
        User currentUser = new User();
        currentUser.setUsername("currentUser");
        currentUser.setRole(Role.SUPER);

        userToUpdate.setRole(Role.ADMIN);

        mockCurrentUser(currentUser);
        when(userRepository.findByEmail("updateuser@email.com")).thenReturn(Optional.of(userToUpdate));

        // Act
        ResponseEntity<Void> response = userService.updateAdministrator("updateuser@email.com", "email", newDetails);

        // Assert
        verify(updateStrategy).update(userToUpdate, newUserDetails, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }



    @Test
    public void testUpdateAdministrator_NoStrategyType() throws IOException {
        // Act
        UserDto input = new UserDto();
        ResponseEntity<Void> response = userService.updateAdministrator("testIdentifier", "INVALID_TYPE", input);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testUpdateAdministrator_UserNotFound_Throw_UserNotFoundException() throws IOException {

        // Arrange
        User currentUser = new User();
        currentUser.setUsername("currentUser");
        currentUser.setRole(Role.ADMIN);

        UserDto input = new UserDto();

        mockCurrentUser(currentUser);

        when(userRepository.findByUsername("sameUser")).thenThrow(new EntityNotFoundException("User not found", null));

        // Act
        ResponseEntity<Void> response = userService.updateAdministrator("nonExistingUser", "username", input);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateAdministrator_NoPermission() throws IOException {
        // Arrange
        currentUser.setUsername("user1");
        currentUser.setRole(Role.READER);

        userToUpdate.setRole(Role.ADMIN);

        UserDto input = new UserDto();

        mockCurrentUser(currentUser);

        when(userService.getUserByIdentifier("1", "id")).thenReturn(userToUpdate);

        // Act
        ResponseEntity<Void> response = userService.updateAdministrator("1", "id", input);

        // Assert
        verify(updateStrategy, never()).update(userToUpdate, new User(input), null);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testUpdateAdministrator_IllegalArgument() throws IOException {
        // Arrange
        mockCurrentUser(currentUser);
        when(userService.getUserByIdentifier("1", "id")).thenThrow(new IllegalArgumentException("Invalid identifier"));

        // Act
        ResponseEntity<Void> response = userService.updateAdministrator("1", "id", new UserDto());

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testChangeUserPassword_Success() {
        // Arrange
        String username = "currentUser";
        String newPassword = "newSecurePassword";

        // Mock the repository to return the current user
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
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }


}
