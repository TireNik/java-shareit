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
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private ItemDto itemDto;
    private CommentDto commentDto;
    private CommentDtoOut commentDtoOut;

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Item 1");
        itemDto.setDescription("Description for Item 1");
        itemDto.setAvailable(true);

        commentDto = new CommentDto();
        commentDto.setText("This is a comment");

        commentDtoOut = new CommentDtoOut(1L, "Comment", "Pasha", LocalDateTime.now(), 1L);
    }

    @Test
    void createItem_success() {
        when(itemService.createItem(any(ItemDto.class), anyLong())).thenReturn(itemDto);

        ResponseEntity<ItemDto> response = itemController.createItem(itemDto, 1L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(itemDto, response.getBody());
        verify(itemService, times(1)).createItem(itemDto, 1L);
    }

    @Test
    void updateItem_success() {
        when(itemService.updateItem(anyLong(), any(ItemDto.class), anyLong())).thenReturn(itemDto);

        ResponseEntity<ItemDto> response = itemController.updateItem(1L, itemDto, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(itemDto, response.getBody());
        verify(itemService, times(1)).updateItem(1L, itemDto, 1L);
    }

    @Test
    void getItemById_success() {
        when(itemService.getItemById(anyLong())).thenReturn(itemDto);

        ResponseEntity<ItemDto> response = itemController.getItem(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(itemDto, response.getBody());
        verify(itemService, times(1)).getItemById(1L);
    }

    @Test
    void getItemsByOwner_success() {
        List<ItemDto> items = Collections.singletonList(itemDto);
        when(itemService.getItemsByOwner(anyLong())).thenReturn(items);

        ResponseEntity<List<ItemDto>> response = itemController.getItemsBuOwner(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(items, response.getBody());
        verify(itemService, times(1)).getItemsByOwner(1L);
    }

    @Test
    void searchItems_success() {
        List<ItemDto> items = Collections.singletonList(itemDto);
        when(itemService.searchItems(anyString())).thenReturn(items);

        ResponseEntity<List<ItemDto>> response = itemController.searchItems("text", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(items, response.getBody());
        verify(itemService, times(1)).searchItems("text");
    }


    @Test
    void createComment_success() {
        when(itemService.createComment(anyLong(), any(CommentDto.class), anyLong())).thenReturn(commentDtoOut);

        ResponseEntity<CommentDtoOut> response = itemController.createComment(1L, commentDto, 1L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(commentDtoOut, response.getBody());
        verify(itemService, times(1)).createComment(1L, commentDto, 1L);
    }

    @Test
    void createComment_noBookings_throwsValidationException() {
        when(itemService.createComment(anyLong(), any(CommentDto.class), anyLong()))
                .thenThrow(new ValidationException("У пользователя с id 1 должно быть хотя бы одно бронирование предмета с id 1"));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            itemController.createComment(1L, commentDto, 1L);
        });

        assertEquals("У пользователя с id 1 должно быть хотя бы одно бронирование предмета с id 1", exception.getMessage());
        verify(itemService, times(1)).createComment(1L, commentDto, 1L);
    }
}