package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {

    @Mock
    private ItemRequestService requestService;

    @InjectMocks
    private ItemRequestController requestController;

    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        itemRequestDto = new ItemRequestDto(
                1L,
                "Нужна дрель",
                LocalDateTime.now(),
                Collections.emptyList()
        );
    }

    @Test
    void createRequest_success() {
        when(requestService.createRequest(any(ItemRequestDto.class), anyLong()))
                .thenReturn(itemRequestDto);

        ResponseEntity<ItemRequestDto> response = requestController.createItem(itemRequestDto, 1L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(itemRequestDto, response.getBody());
        verify(requestService, times(1)).createRequest(itemRequestDto, 1L);
    }

    @Test
    void getUserRequests_success() {
        List<ItemRequestDto> requests = Collections.singletonList(itemRequestDto);
        when(requestService.getUserRequests(anyLong()))
                .thenReturn(requests);

        ResponseEntity<List<ItemRequestDto>> response = requestController.getItemsBuOwner(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(requests, response.getBody());
        verify(requestService, times(1)).getUserRequests(1L);
    }

    @Test
    void getAllRequests_success() {
        List<ItemRequestDto> requests = Collections.singletonList(itemRequestDto);
        when(requestService.getAllRequests())
                .thenReturn(requests);

        ResponseEntity<List<ItemRequestDto>> response = requestController.getAllRequests();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(requests, response.getBody());
        verify(requestService, times(1)).getAllRequests();
    }

    @Test
    void getRequestById_success() {
        when(requestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(itemRequestDto);

        ResponseEntity<ItemRequestDto> response = requestController.getItem(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(itemRequestDto, response.getBody());
        verify(requestService, times(1)).getRequestById(1L, 1L);
    }
}