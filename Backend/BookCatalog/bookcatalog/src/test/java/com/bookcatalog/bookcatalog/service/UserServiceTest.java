package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.model.dto.UserShortDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;
    private SecurityContext securityContext;
    private Authentication authentication;
    private UserDto currentUser;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        currentUser = new UserDto();
        when(authentication.getPrincipal()).thenReturn(currentUser);
    }

    @Test
    public void testGetUsersShortList_AllUSersNonSuper_Success() {

        // Arrange
        User readerUser = new User("reader", "reader@example.com", "password", Role.READER);
        User adminUser = new User("admin", "admin@example.com", "password", Role.ADMIN);

        when(userRepository.findAll()).thenReturn(Arrays.asList(readerUser, adminUser));

        // Act
        List<UserShortDto> result = userService.getUsersShortList();

        // Assert
        assertEquals(2, result.size());

        UserShortDto readerUserDto = result.get(0);
        assertEquals("reader", readerUserDto.getUsername());
        assertEquals("reader@example.com", readerUserDto.getEmail());
        assertEquals(Role.READER, readerUserDto.getRole());

        UserShortDto adminUserDto = result.get(1);
        assertEquals("admin", adminUserDto.getUsername());
        assertEquals("admin@example.com", adminUserDto.getEmail());
        assertEquals(Role.ADMIN, adminUserDto.getRole());
    }

    @Test
    public void testGetUsersShortList_WithOneUSerSuper_ShouldReturnOnlyNonSuperUsers_Success() {

        // Arrange
        User readerUser1 = new User("reader1", "reader1@example.com", "password", Role.READER);
        User adminUser1 = new User("admin1", "admin1@example.com", "password", Role.ADMIN);
        User superUser = new User("super", "super@example.com", "password", Role.SUPER);
        User readerUser2 = new User("reader2", "reader2@example.com", "password", Role.READER);
        User adminUser2 = new User("admin2", "admin2@example.com", "password", Role.ADMIN);

        when(userRepository.findAll()).thenReturn(Arrays.asList(readerUser1, adminUser1, superUser, readerUser2, adminUser2));

        // Act
        List<UserShortDto> result = userService.getUsersShortList();

        // Assert
        assertEquals(4, result.size());

        UserShortDto readerUserDto1 = result.getFirst();
        assertEquals("reader1", readerUserDto1.getUsername());
        assertEquals("reader1@example.com", readerUserDto1.getEmail());
        assertEquals(Role.READER, readerUserDto1.getRole());

        UserShortDto adminUserDto1 = result.get(1);
        assertEquals("admin1", adminUserDto1.getUsername());
        assertEquals("admin1@example.com", adminUserDto1.getEmail());
        assertEquals(Role.ADMIN, adminUserDto1.getRole());

        UserShortDto readerUserDto2 = result.get(2);
        assertEquals("reader2", readerUserDto2.getUsername());
        assertEquals("reader1@example.com", readerUserDto1.getEmail());
        assertEquals(Role.READER, readerUserDto1.getRole());

        UserShortDto adminUserDto2 = result.get(3);
        assertEquals("admin2", adminUserDto2.getUsername());
        assertEquals("admin2@example.com", adminUserDto2.getEmail());
        assertEquals(Role.ADMIN, adminUserDto2.getRole());
    }

    @Test
    public void testCreateAdministrator() {

        // Arrange
        RegisterUserDto input = new RegisterUserDto();
        input.setUsername("adminUser");
        input.setEmail("admin@example.com");
        input.setPassword("password");

        User expectedUser = new User("adminUser", "admin@example.com", "encodedPassword", Role.ADMIN);

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        // Act
        User result = userService.createAdministrator(input);

        // Assert
        assertEquals(expectedUser.getUsername(), result.getUsername());
        assertEquals(expectedUser.getEmail(), result.getEmail());
        assertEquals(expectedUser.getPassword(), result.getPassword());
        assertEquals(expectedUser.getRole(), result.getRole());

        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testDeleteAdministratorById_UserIsAdmin_ReturnsOk() {

        // Arrange
        User adminUser = new User("adminUser", "admin@example.com", "password", Role.ADMIN);
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(adminUser));

        // Act
        ResponseEntity<Void> response = userService.deleteAdministratorById(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).deleteById(1);
    }

    @Test
    public void testDeleteAdministratorById_UserIsNotAdmin_ReturnsForbidden() {

        // Arrange
        User nonAdminUser = new User("user", "user@example.com", "password", Role.READER);
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(nonAdminUser));

        // Act
        ResponseEntity<Void> response = userService.deleteAdministratorById(1);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    public void testDeleteAdministratorById_UserNotFound_ReturnsNotFound() {

        // Arrange
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = userService.deleteAdministratorById(1);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    public void testDeleteAdministratorByUsernameOrEmail_UserFoundByUsernameAndIsAdmin() {
        // Arrange
        User adminUser = new User();
        adminUser.setUsername("adminUser");
        adminUser.setRole(Role.ADMIN);
        adminUser.setId(1);

        when(userRepository.findByUsername("adminUser")).thenReturn(Optional.of(adminUser));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = userService.deleteAdministratorByUsernameOrEmail("adminUser");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).deleteById(adminUser.getId());
    }

    @Test
    public void testDeleteAdministratorByUsernameOrEmail_UserFoundByEmailAndIsAdmin() {
        // Arrange
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);
        adminUser.setId(1);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // Act
        ResponseEntity<Void> response = userService.deleteAdministratorByUsernameOrEmail("admin@example.com");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).deleteById(adminUser.getId());
    }

    @Test
    public void testDeleteAdministratorByUsernameOrEmail_UserNotAdmin() {
        // Arrange
        User nonAdminUser = new User();
        nonAdminUser.setUsername("user");
        nonAdminUser.setRole(Role.READER);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(nonAdminUser));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = userService.deleteAdministratorByUsernameOrEmail("user");

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    public void testDeleteAdministratorByUsernameOrEmail_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = userService.deleteAdministratorByUsernameOrEmail("nonexistent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    public void testGetUserById_CurrentUserIsNull() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(null);

        // Act
        Optional<UserDto> result = userService.getUserById(1);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findById(anyInt());
    }

    @Test
    public void testGetUserById_CurrentUserNotReaderOrGuest() {
        // Arrange
        when(currentUser.getRole()).thenReturn(Role.ADMIN);
        when(userService.getCurrentUser()).thenReturn(currentUser);

        // Act
        Optional<UserDto> result = userService.getUserById(1);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findById(anyInt());
    }

    @Test
    public void testGetUserById_UserFoundById_RestrictedRoles() {
        // Arrange
        User foundUser = new User();
        foundUser.setRole(Role.ADMIN);
        when(currentUser.getRole()).thenReturn(Role.READER);
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.findById(1)).thenReturn(Optional.of(foundUser));

        // Act
        Optional<UserDto> result = userService.getUserById(1);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository).findById(1);
    }

    @Test
    public void testGetUserById_UserFoundById_NonRestrictedRoles() {
        // Arrange
        User foundUser = new User();
        foundUser.setRole(Role.READER);
        UserDto foundUserDto = new UserDto();
        foundUserDto.setRole(Role.READER);

        when(currentUser.getRole()).thenReturn(Role.READER);
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.findById(1)).thenReturn(Optional.of(foundUser));
        when(userService.fromUserToUserDto(foundUser)).thenReturn(foundUserDto);

        // Act
        Optional<UserDto> result = userService.getUserById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(foundUserDto, result.get());
        verify(userRepository).findById(1);
    }

    @Test
    public void testGetUserById_UserFoundById_CurrentUserNonRestrictedRole() {
        // Arrange
        User foundUser = new User();
        foundUser.setRole(Role.READER);
        UserDto foundUserDto = new UserDto();
        foundUserDto.setRole(Role.READER);

        when(currentUser.getRole()).thenReturn(Role.ADMIN);
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.findById(1)).thenReturn(Optional.of(foundUser));
        when(userService.fromUserToUserDto(foundUser)).thenReturn(foundUserDto);

        // Act
        Optional<UserDto> result = userService.getUserById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(foundUserDto, result.get());
        verify(userRepository).findById(1);
    }

    @Test
    public void testGetUserById_UserNotFoundById() {
        // Arrange
        when(currentUser.getRole()).thenReturn(Role.READER);
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        Optional<UserDto> result = userService.getUserById(1);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository).findById(1);
    }



    // -----------------------------

    /*
    @Test
    public void testGetUserById_UserNotFound() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        Optional<UserDto> result = userService.getUserById(1);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(1);
    }
*/
    @Test
    public void testGetUserByUsernameOrEmail_UserDoesNotExist_ReturnsEmpty() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Optional<UserDto> result = userService.getUserByUsernameOrEmail("nonexistent");

        assertEquals(Optional.empty(), result);
    }

}