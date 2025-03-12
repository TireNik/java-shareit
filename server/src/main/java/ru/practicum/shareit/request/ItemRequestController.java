package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {

    private final ItemRequestService requestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> createItem(
            @RequestBody @Valid ItemRequestDto dto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Добавляем запрос вещи: {}", dto);
        ItemRequestDto requestDto = requestService.createRequest(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(requestDto);
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getItemsBuOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemRequestDto> itemRequests = requestService.getUserRequests(userId);
        return ResponseEntity.ok(itemRequests);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemRequestDto> itemRequests = requestService.getAllRequests(userId);
        return ResponseEntity.ok(itemRequests);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getItem(
            @PathVariable Long requestId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        ItemRequestDto itemRequest = requestService.getRequestById(requestId, userId);
        return ResponseEntity.ok(itemRequest);
    }
}
