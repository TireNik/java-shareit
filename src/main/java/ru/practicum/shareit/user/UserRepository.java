package ru.practicum.shareit.user;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.incrementAndGet());
        }
        for (User existingUser : users.values()) {
            if (existingUser.getEmail().equals(user.getEmail())) {
                throw new ValidationException("Пользователь с таким email уже существует");
            }
        }
        users.put(user.getId(), user);
        return user;
    }

    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public void deleteById(Long userId) {
        users.remove(userId);
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
}
