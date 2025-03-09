package ru.practicum.shareit.user;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(String name, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        User savedUser = userRepository.save(user);
        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Override
    public UserDto updateUser(Long userId, UserDto upUserDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        if (upUserDto.getName() != null && !upUserDto.getName().isBlank()) {
            user.setName(upUserDto.getName());
        }
        if (upUserDto.getEmail() != null && !upUserDto.getEmail().isBlank()) {
            if (userRepository.existsByEmail(upUserDto.getEmail())) {
                throw new ValidationException("Email уже используется другим пользователем");
            }
            user.setEmail(upUserDto.getEmail());
        }
        return UserMapper.toDto(userRepository.save(user));
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
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    private User getUserByIdEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));
    }
}
