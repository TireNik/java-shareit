package ru.practicum.shareit.user;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        try {
            User savedUser = userRepository.save(user);
            return userMapper.toDto(savedUser);
        } catch (Exception e) {
            log.info("createUser ERROR", e.getMessage());
            throw new ValidationException("createUser ERROR");
        }
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = getUserByIdEntity(userId);
        return userMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto upUserDto) {
        User user = getUserByIdEntity(userId);
        if (upUserDto.getName() != null && !upUserDto.getName().isBlank()) {
            user.setName(upUserDto.getName());
        }
        if (upUserDto.getEmail() != null && !upUserDto.getEmail().isBlank()) {
            Optional<User> existingUser = userRepository.findAll().stream()
                    .filter(u -> u.getEmail().equals(upUserDto.getEmail()) && !u.getId().equals(userId))
                    .findFirst();
            if (existingUser.isPresent()) {
                throw new ValidationException("Email уже используется другим пользователем");
            }
            user.setEmail(upUserDto.getEmail());
        }
        try {
            User updatedUser = userRepository.updateUser(user);
            return userMapper.toDto(updatedUser);
        } catch (Exception e) {
            log.info("updateUser ERROR", e.getMessage());
            throw new ValidationException("updateUser ERROR");
        }
    }

    @Override
    public void deleteUser(Long userId) {
        getUserByIdEntity(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    private User getUserByIdEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));
    }
}
