package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;

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

        return enrichWithItems(itemRequests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        findUserById(userId);
        List<ItemRequest> itemRequests = requestRepository.findAllExceptUser(userId);
        return enrichWithItems(itemRequests);
    }

    private List<ItemRequestDto> enrichWithItems(List<ItemRequest> itemRequests) {
        List<Long> requestIds = itemRequests.stream()
                .map(ItemRequest::getId)
                .toList();

        List<Item> items = itemRepository.findByRequestIds(requestIds);

        Map<Long, List<ItemDto>> itemsMap = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(ItemMapper::toDto, Collectors.toList())
                ));

        return itemRequests.stream()
                .map(request -> {
                    ItemRequestDto dto = RequestMapper.toDto(request);
                    dto.setItems(itemsMap.getOrDefault(request.getId(), Collections.emptyList()));
                    return dto;
                })
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