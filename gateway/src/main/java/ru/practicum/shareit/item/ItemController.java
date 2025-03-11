package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(
            @RequestBody @Valid ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") @NotNull Long userId) {
        log.info("Создание новой вещи: {}", itemDto);
        return itemClient.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") @NotNull Long userId) {
        log.info("Редактирование вещи с ID {} пользователем с ID {}", itemId, userId);
        return itemClient.update(itemDto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(
            @PathVariable @NotNull Long itemId,
            @RequestHeader("X-Sharer-User-Id") @NotNull Long userId) {
        log.info("Получение информации о вещи с ID {} пользователем с ID {}", itemId, userId);
        return itemClient.get(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.getItemsByOwner(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(
            @RequestParam("text") String text,
            @RequestHeader("X-Sharer-User-Id") @NotNull Long userId) {
        log.info("Поиск вещей с текстом '{}' пользователем с ID {}", text, userId);
        return itemClient.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(
            @RequestHeader("X-Sharer-User-Id") @NotNull Long userId,
            @Valid @RequestBody CommentDto dto,
            @PathVariable @NotNull Long itemId) {
        log.info("Добавление комментария к вещи {} пользователем {}", itemId, userId);
        return itemClient.createComment(userId, dto, itemId);
    }
}
