package ru.practicum.shareit;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ShareItApp.class)
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(UserControllerIntegrationTest.class);
    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private String baseUrl;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        HttpClient httpClient = HttpClients.createDefault(); // HttpClient 5.x
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplateBuilder builder = new RestTemplateBuilder().requestFactory(() -> factory); // Настраиваем через RestTemplateBuilder
        restTemplate = new TestRestTemplate(builder); // Передаём builder в TestRestTemplate
        baseUrl = "http://localhost:" + port + "/users";

        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void createUser_success() {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john.doe@example.com");

        ResponseEntity<UserDto> response = restTemplate.postForEntity(baseUrl, userDto, UserDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserDto createdUser = response.getBody();
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("John Doe", createdUser.getName());
        assertEquals("john.doe@example.com", createdUser.getEmail());
    }

    @Test
    void createUser_duplicateEmail_returnsConflict() {
        UserDto userDto1 = new UserDto();
        userDto1.setName("John Doe");
        userDto1.setEmail("john.doe@example.com");
        restTemplate.postForEntity(baseUrl, userDto1, UserDto.class);

        UserDto userDto2 = new UserDto();
        userDto2.setName("Jane Doe");
        userDto2.setEmail("john.doe@example.com");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, userDto2, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void getUserById_success() {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john.doe@example.com");
        ResponseEntity<UserDto> createResponse = restTemplate.postForEntity(baseUrl, userDto, UserDto.class);
        Long userId = createResponse.getBody().getId();

        ResponseEntity<UserDto> response = restTemplate.getForEntity(baseUrl + "/" + userId, UserDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDto retrievedUser = response.getBody();
        assertNotNull(retrievedUser);
        assertEquals(userId, retrievedUser.getId());
        assertEquals("John Doe", retrievedUser.getName());
        assertEquals("john.doe@example.com", retrievedUser.getEmail());
    }

    @Test
    void getUserById_notFound_returns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/999", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateUser_success() {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john.doe@example.com");
        ResponseEntity<UserDto> createResponse = restTemplate.postForEntity(baseUrl, userDto, UserDto.class);
        Long userId = createResponse.getBody().getId();

        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail("updated.email@example.com");

        ResponseEntity<UserDto> response = restTemplate.exchange(
                baseUrl + "/" + userId,
                HttpMethod.PATCH,
                new HttpEntity<>(updateDto),
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDto updatedUser = response.getBody();
        assertNotNull(updatedUser);
        assertEquals(userId, updatedUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated.email@example.com", updatedUser.getEmail());
    }

    @Test
    void updateUser_notFound_returns404() {
        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Name");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/999",
                HttpMethod.PATCH,
                new HttpEntity<>(updateDto),
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteUser_success() {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john.doe@example.com");
        ResponseEntity<UserDto> createResponse = restTemplate.postForEntity(baseUrl, userDto, UserDto.class);
        Long userId = createResponse.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + userId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Проверка, что пользователь удалён
        ResponseEntity<String> getResponse = restTemplate.getForEntity(baseUrl + "/" + userId, String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    void getAllUsers_success() {
        UserDto userDto1 = new UserDto();
        userDto1.setName("John Doe");
        userDto1.setEmail("john.doe@example.com");
        restTemplate.postForEntity(baseUrl, userDto1, UserDto.class);

        UserDto userDto2 = new UserDto();
        userDto2.setName("Jane Doe");
        userDto2.setEmail("jane.doe@example.com");
        restTemplate.postForEntity(baseUrl, userDto2, UserDto.class);

        ResponseEntity<List> response = restTemplate.getForEntity(baseUrl, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<?> users = response.getBody();
        assertNotNull(users);
        assertEquals(2, users.size());
    }
}