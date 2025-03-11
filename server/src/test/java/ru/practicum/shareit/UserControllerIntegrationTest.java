package ru.practicum.shareit;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import ru.practicum.shareit.user.dto.UserDto;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ShareItApp.class)
@ActiveProfiles("test")
@Slf4j
class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private String baseUrl;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        HttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplateBuilder builder = new RestTemplateBuilder().requestFactory(() -> factory);
        restTemplate = new TestRestTemplate(builder);
        baseUrl = "http://localhost:" + port + "/users";

        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void createUser_success() {
        UserDto userDto = new UserDto();
        userDto.setName("Pasha Technic");
        userDto.setEmail("pasha.technic@example.com");

        ResponseEntity<UserDto> response = restTemplate.postForEntity(baseUrl, userDto, UserDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserDto createdUser = response.getBody();
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("Pasha Technic", createdUser.getName());
        assertEquals("pasha.technic@example.com", createdUser.getEmail());
    }

    @Test
    void createUser_duplicateEmail_returnsConflict() {
        UserDto userDto1 = new UserDto();
        userDto1.setName("Pasha Technic");
        userDto1.setEmail("pasha.technic@example.com");
        restTemplate.postForEntity(baseUrl, userDto1, UserDto.class);

        UserDto userDto2 = new UserDto();
        userDto2.setName("Pasha Technic");
        userDto2.setEmail("pasha.technic@example.com");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, userDto2, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void getUserById_success() {
        UserDto userDto = new UserDto();
        userDto.setName("Pasha Technic");
        userDto.setEmail("pasha.technic@example.com");
        ResponseEntity<UserDto> createResponse = restTemplate.postForEntity(baseUrl, userDto, UserDto.class);
        Long userId = createResponse.getBody().getId();

        ResponseEntity<UserDto> response = restTemplate.getForEntity(baseUrl + "/" + userId, UserDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDto retrievedUser = response.getBody();
        assertNotNull(retrievedUser);
        assertEquals(userId, retrievedUser.getId());
        assertEquals("Pasha Technic", retrievedUser.getName());
        assertEquals("pasha.technic@example.com", retrievedUser.getEmail());
    }

    @Test
    void getUserById_notFound_returns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/999", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateUser_success() {
        UserDto userDto = new UserDto();
        userDto.setName("Pasha Technic");
        userDto.setEmail("pasha.technic@example.com");
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
        userDto.setName("Pasha Technic");
        userDto.setEmail("pasha.technic@example.com");
        ResponseEntity<UserDto> createResponse = restTemplate.postForEntity(baseUrl, userDto, UserDto.class);
        Long userId = createResponse.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + userId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ResponseEntity<String> getResponse = restTemplate.getForEntity(baseUrl + "/" + userId, String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
}