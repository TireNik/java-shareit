package ru.practicum.shareit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@RequiredArgsConstructor
class ItemRequestControllerTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private String baseUrl;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    private User user;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        HttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplateBuilder builder = new RestTemplateBuilder().requestFactory(() -> factory);
        restTemplate = new TestRestTemplate(builder);
        baseUrl = "http://localhost:" + port + "/requests";

        requestRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(new User(null, "Иван", "ivan@test.com"));
        request = requestRepository.save(new ItemRequest(null, user, "Нужен молоток", LocalDateTime.now(), new ArrayList<>()));
    }

    @Test
    void createItem_success() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Ищу отвертку", LocalDateTime.now(), Collections.emptyList());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(requestDto, createHeaders(user.getId())),
                String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertEquals(requestDto.getDescription(), jsonResponse.get("description").asText());
    }


    @Test
    void createItem_missingUserId() {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Ищу отвертку", LocalDateTime.now(), Collections.emptyList());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(requestDto),
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getItemsBuOwner_success() {
        ResponseEntity<ItemRequestDto[]> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(user.getId())),
                ItemRequestDto[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals(request.getDescription(), response.getBody()[0].getDescription());
    }

    @Test
    void getItem_success() {
        ResponseEntity<ItemRequestDto> response = restTemplate.exchange(
                baseUrl + "/" + request.getId(),
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(user.getId())),
                ItemRequestDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(request.getId(), response.getBody().getId());
        assertEquals(request.getDescription(), response.getBody().getDescription());
    }


    private HttpHeaders createHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
