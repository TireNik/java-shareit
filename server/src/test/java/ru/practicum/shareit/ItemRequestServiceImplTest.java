package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository requestRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private User user;
    private ItemRequestDto requestDto;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Test User", "test@example.com");
        requestDto = new ItemRequestDto(1L, "Need a drill", LocalDateTime.now(), Collections.emptyList());
        request = new ItemRequest(1L, user, "Need a drill", LocalDateTime.now(), new ArrayList<>());
    }

    @Test
    void createRequest_success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);

        ItemRequestDto result = requestService.createRequest(requestDto, 1L);

        assertNotNull(result);
        assertEquals(requestDto.getDescription(), result.getDescription());
        verify(requestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void createRequest_userNotFound_throwsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> requestService.createRequest(requestDto, 1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void getUserRequests_userNotFound_throwsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> requestService.getUserRequests(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(requestRepository, never()).findAllByUserIdOrderByCreatedDesc(anyLong());
    }


    @Test
    void getRequestById_success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.of(request));

        ItemRequestDto result = requestService.getRequestById(1L, 1L);

        assertNotNull(result);
        assertEquals(request.getDescription(), result.getDescription());
    }

    @Test
    void getRequestById_requestNotFound_throwsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(1L, 1L));

        assertEquals("Запрос с id:1 не найден", exception.getMessage());
    }

    @Test
    void getRequestById_userNotFound_throwsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> requestService.getRequestById(1L, 1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}