package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    public ItemRequestDto createRequest(ItemRequestDto dto, Long userId) {
        User user = findUserById(userId);

        ItemRequest itemRequest =  RequestMapper.toEntity(dto, user);

        requestRepository.save(itemRequest);
        return RequestMapper.toDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        findUserById(userId);
        List<ItemRequest> itemRequests = requestRepository.findAllByUserIdOrderByCreatedDesc(userId);
        return itemRequests.stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAllRequests() {
        List<ItemRequest> itemRequests = requestRepository.findAll();
        return itemRequests.stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        findUserById(userId);

        ItemRequest itemRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id:" + requestId + " не найден"));

        ItemRequestDto dto = RequestMapper.toDto(itemRequest);
        return dto;
    }


    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

}