package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {

    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createItem(
            @RequestBody @Valid RequestDto dto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Добавляем запрос вещи: {}", dto);
        return requestClient.createRequest(dto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsBuOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestClient.getUserRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests() {
        return requestClient.getAll();
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItem(
            @PathVariable @NotNull Long requestId,
            @RequestHeader("X-Sharer-User-Id") @NotNull Long userId) {
        return requestClient.getRequestById(requestId, userId);
    }
}
