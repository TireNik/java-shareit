package ru.practicum.shareit;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Pasha Technic");
        userDto.setEmail("pasha.technic@example.com");
    }

    @Test
    void createUser_success() {
        when(userService.createUser(anyString(), anyString())).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.createUser(userDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userDto, response.getBody());
        verify(userService, times(1)).createUser("Pasha Technic", "pasha.technic@example.com");
    }

    @Test
    void createUser_emailAlreadyExists_throwsValidationException() {
        when(userService.createUser(anyString(), anyString()))
                .thenThrow(new ValidationException("Пользователь с таким email уже существует"));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userController.createUser(userDto);
        });

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
        verify(userService, times(1)).createUser("Pasha Technic", "pasha.technic@example.com");
    }

    @Test
    void getUserById_success() {
        when(userService.getUserById(1L)).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto, response.getBody());
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getUserById_notFound_throwsNotFoundException() {
        when(userService.getUserById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userController.getUserById(999L);
        });

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userService, times(1)).getUserById(999L);
    }

    @Test
    void updateUser_success() {
        UserDto updatedDto = new UserDto();
        updatedDto.setId(1L);
        updatedDto.setName("Updated Name");
        updatedDto.setEmail("updated.email@example.com");

        when(userService.updateUser(anyLong(), any(UserDto.class))).thenReturn(updatedDto);

        ResponseEntity<UserDto> response = userController.updateUser(1L, updatedDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedDto, response.getBody());
        verify(userService, times(1)).updateUser(1L, updatedDto);
    }

    @Test
    void updateUser_notFound_throwsValidationException() {
        when(userService.updateUser(anyLong(), any(UserDto.class)))
                .thenThrow(new ValidationException("Пользователь не найден"));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userController.updateUser(999L, userDto);
        });

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userService, times(1)).updateUser(999L, userDto);
    }

    @Test
    void deleteUser_success() {
        doNothing().when(userService).deleteUser(anyLong());

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void getAllUsers_success() {
        List<UserDto> users = Collections.singletonList(userDto);
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserDto>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getAllUsers_emptyList() {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        ResponseEntity<List<UserDto>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(userService, times(1)).getAllUsers();
    }
}