package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idGenerator.incrementAndGet());
        }
        items.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    public List<Item> findAllByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .toList();
    }

    public List<Item> searchItemsByTxt(String txt) {
        return items.values().stream()
                .filter(item -> item.getDescription().toLowerCase().contains(txt)
                        || item.getName().toLowerCase().contains(txt)
                        && Boolean.TRUE.equals(item.getAvailable()))
                .collect(Collectors.toList());
    }
}
