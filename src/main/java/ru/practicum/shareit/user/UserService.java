package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(String name, String email);

    UserDto getUserById(Long userId);

    UserDto updateUser(Long userId, User upUser);

    void deleteUser(Long userId);

    List<UserDto> getAllUsers();
}
