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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ShareItApp.class)
@Slf4j
class ItemControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private String itemBaseUrl;
    private String userBaseUrl;
    private String bookingBaseUrl;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long userId;
    private Long itemId;
    private Integer bookingId;

    @BeforeEach
    void setUp() {
        HttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplateBuilder builder = new RestTemplateBuilder().requestFactory(() -> factory);
        restTemplate = new TestRestTemplate(builder);

        itemBaseUrl = "http://localhost:" + port + "/items";
        userBaseUrl = "http://localhost:" + port + "/users";
        bookingBaseUrl = "http://localhost:" + port + "/bookings";

        jdbcTemplate.update("DELETE FROM comments");
        jdbcTemplate.update("DELETE FROM bookings");
        jdbcTemplate.update("DELETE FROM items");
        jdbcTemplate.update("DELETE FROM item_requests");
        jdbcTemplate.update("DELETE FROM users");

        UserDto userDto = new UserDto(null, "Pasha Technic", "pasha.technic@example.com");
        ResponseEntity<UserDto> userResponse = restTemplate.postForEntity(userBaseUrl, userDto, UserDto.class);
        userId = Objects.requireNonNull(userResponse.getBody()).getId();
        log.info("Создание пользователя с ID: {}", userId);

        ItemDto itemDto = new ItemDto(null, "Item 1", "Description for Item 1", true,
                null, null, null, null);
        ResponseEntity<ItemDto> itemResponse = restTemplate.postForEntity(
                itemBaseUrl,
                new HttpEntity<>(itemDto, createHeadersWithUserId(userId)),
                ItemDto.class);
        assertEquals(HttpStatus.CREATED, itemResponse.getStatusCode(), "Ошибка создания Вещи: " + itemResponse.getBody());
        itemId = Objects.requireNonNull(itemResponse.getBody()).getId();
        log.info("Создание вещи с ID: {}", itemId);
    }

    @Test
    void createItem_success() {
        ItemDto itemDto = new ItemDto(null, "New Item", "Description for New Item", true,
                null, null, null, null);
        ResponseEntity<ItemDto> response = restTemplate.postForEntity(
                itemBaseUrl,
                new HttpEntity<>(itemDto, createHeadersWithUserId(userId)),
                ItemDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Ошибка создания Вещи: " + response.getBody());
        ItemDto createdItem = response.getBody();
        assertNotNull(createdItem);
        assertNotNull(createdItem.getId());
        assertEquals("New Item", createdItem.getName());
        assertEquals("Description for New Item", createdItem.getDescription());
        assertTrue(createdItem.getAvailable());
        log.info("Created item: {}", createdItem);
    }

    @Test
    void updateItem_success() {
        ItemDto updateDto = new ItemDto(null, "Updated Item", "Updated Description", null,
                null, null, null, null);
        ResponseEntity<ItemDto> response = restTemplate.exchange(
                itemBaseUrl + "/" + itemId,
                HttpMethod.PATCH,
                new HttpEntity<>(updateDto, createHeadersWithUserId(userId)),
                ItemDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Ошибка обновления Вещи: " + response.getBody());
        ItemDto updatedItem = response.getBody();
        assertNotNull(updatedItem);
        assertEquals(itemId, updatedItem.getId());
        assertEquals("Updated Item", updatedItem.getName());
        assertEquals("Updated Description", updatedItem.getDescription());
        assertTrue(updatedItem.getAvailable());
        log.info("Обнавление вещи: {}", updatedItem);
    }

    @Test
    void getItemById_success() {
        ResponseEntity<ItemDto> response = restTemplate.exchange(
                itemBaseUrl + "/" + itemId,
                HttpMethod.GET,
                new HttpEntity<>(createHeadersWithUserId(userId)),
                ItemDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Ошибка получения вещи: " + response.getBody());
        ItemDto retrievedItem = response.getBody();
        assertNotNull(retrievedItem);
        assertEquals(itemId, retrievedItem.getId());
        assertEquals("Item 1", retrievedItem.getName());
        assertEquals("Description for Item 1", retrievedItem.getDescription());
        assertTrue(retrievedItem.getAvailable());
        assertNull(retrievedItem.getLastBooking());
        assertNull(retrievedItem.getNextBooking());
        assertTrue(retrievedItem.getComments().isEmpty());
        log.info("Полученная штука: {}", retrievedItem);
    }

    @Test
    void getItemById_withBookingAndComment() {
        // Создание второго пользователя (арендатор)
        UserDto bookerDto = new UserDto(null, "Alex Booker", "alex.booker@example.com");
        ResponseEntity<UserDto> bookerResponse = restTemplate.postForEntity(userBaseUrl, bookerDto, UserDto.class);
        assertEquals(HttpStatus.CREATED, bookerResponse.getStatusCode(), "Ошибочка создания владельца: " + bookerResponse.getBody());
        Long bookerId = Objects.requireNonNull(bookerResponse.getBody()).getId();

        // Создание бронирования
        BookingDto bookingDto = new BookingDto(itemId, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        ResponseEntity<BookingDtoOut> bookingResponse = restTemplate.postForEntity(
                bookingBaseUrl,
                new HttpEntity<>(bookingDto, createHeadersWithUserId(bookerId)),
                BookingDtoOut.class);
        assertEquals(HttpStatus.CREATED, bookingResponse.getStatusCode(), "Ошибка бронирования: " + bookingResponse.getBody());
        bookingId = Math.toIntExact(Objects.requireNonNull(bookingResponse.getBody()).getId());

        // Подтверждение бронирования владельцем
        ResponseEntity<BookingDtoOut> confirmResponse = restTemplate.exchange(
                bookingBaseUrl + "/" + bookingId + "?approved=true",
                HttpMethod.PATCH,
                new HttpEntity<>(createHeadersWithUserId(userId)),
                BookingDtoOut.class);
        assertEquals(HttpStatus.OK, confirmResponse.getStatusCode(), "Ошибка подтверждения броони: " + confirmResponse.getBody());
        assertEquals(BookingStatus.APPROVED, confirmResponse.getBody().getStatus());

        // Создание комментария
        CommentDto commentDto = new CommentDto("Great item!");
        ResponseEntity<CommentDtoOut> commentResponse = restTemplate.postForEntity(
                itemBaseUrl + "/" + itemId + "/comment",
                new HttpEntity<>(commentDto, createHeadersWithUserId(bookerId)),
                CommentDtoOut.class);
        assertEquals(HttpStatus.CREATED, commentResponse.getStatusCode(), "Ошибка создания коммена: " + commentResponse.getBody());

        // Получение вещи
        ResponseEntity<ItemDto> response = restTemplate.exchange(
                itemBaseUrl + "/" + itemId,
                HttpMethod.GET,
                new HttpEntity<>(createHeadersWithUserId(userId)),
                ItemDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Ошибка получения вещи: " + response.getBody());
        ItemDto retrievedItem = response.getBody();
        assertNotNull(retrievedItem);
        assertEquals(itemId, retrievedItem.getId());
        assertNull(retrievedItem.getNextBooking());
        assertEquals("Great item!", retrievedItem.getComments().get(0).getText());
        assertEquals("Alex Booker", retrievedItem.getComments().get(0).getAuthorName());
        assertEquals(itemId, retrievedItem.getComments().get(0).getItemId());
        log.info("вещь с бронью и комментом: {}", retrievedItem);
    }

    @Test
    void getItemsByOwner_success() {
        ResponseEntity<ItemDto[]> response = restTemplate.exchange(
                itemBaseUrl,
                HttpMethod.GET,
                new HttpEntity<>(createHeadersWithUserId(userId)),
                ItemDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Ошибка получения вещи: " + Arrays.toString(response.getBody()));
        ItemDto[] items = response.getBody();
        assertNotNull(items);
        assertEquals(1, items.length);
        assertEquals("Item 1", items[0].getName());
        assertEquals("Description for Item 1", items[0].getDescription());
        assertTrue(items[0].getAvailable());
        assertNull(items[0].getLastBooking());
        assertNull(items[0].getNextBooking());
        assertTrue(items[0].getComments().isEmpty());
        log.info("Вещь владельца: {}", Arrays.toString(items));
    }

    @Test
    void searchItems_success() {
        ResponseEntity<ItemDto[]> response = restTemplate.exchange(
                itemBaseUrl + "/search?text=Item",
                HttpMethod.GET,
                new HttpEntity<>(createHeadersWithUserId(userId)),
                ItemDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Вещь не найдена: " + Arrays.toString(response.getBody()));
        ItemDto[] items = response.getBody();
        assertNotNull(items);
        assertEquals(1, items.length);
        assertEquals("Item 1", items[0].getName());
        assertEquals("Description for Item 1", items[0].getDescription());
        assertTrue(items[0].getAvailable());
        log.info("Найденыш: {}", Arrays.toString(items));
    }

    @Test
    void createComment_success() {
        UserDto bookerDto = new UserDto(null, "Alex Booker", "alex.booker@example.com");
        ResponseEntity<UserDto> bookerResponse = restTemplate.postForEntity(
                userBaseUrl, bookerDto, UserDto.class);
        Long bookerId = Objects.requireNonNull(bookerResponse.getBody()).getId();

        BookingDto bookingDto = new BookingDto(itemId, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        ResponseEntity<BookingDtoOut> bookingResponse = restTemplate.postForEntity(
                bookingBaseUrl,
                new HttpEntity<>(bookingDto, createHeadersWithUserId(bookerId)),
                BookingDtoOut.class);
        bookingId = Math.toIntExact(Objects.requireNonNull(bookingResponse.getBody()).getId());

        ResponseEntity<BookingDtoOut> confirmResponse = restTemplate.exchange(
                bookingBaseUrl + "/" + bookingId + "?approved=true",
                HttpMethod.PATCH,
                new HttpEntity<>(createHeadersWithUserId(userId)),
                BookingDtoOut.class);
        assertEquals(HttpStatus.OK, confirmResponse.getStatusCode());

        CommentDto commentDto = new CommentDto("This is a comment");
        ResponseEntity<CommentDtoOut> response = restTemplate.postForEntity(
                itemBaseUrl + "/" + itemId + "/comment",
                new HttpEntity<>(commentDto, createHeadersWithUserId(bookerId)),
                CommentDtoOut.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        CommentDtoOut createdComment = response.getBody();
        assertNotNull(createdComment);
        assertNotNull(createdComment.getId());
        assertEquals("This is a comment", createdComment.getText());
        assertEquals("Alex Booker", createdComment.getAuthorName());
        assertEquals(itemId, createdComment.getItemId());
        assertNotNull(createdComment.getCreated());
    }

    @Test
    void createComment_noBooking_returns400() {
        CommentDto commentDto = new CommentDto("This is a comment");

        ResponseEntity<String> response = restTemplate.postForEntity(
                itemBaseUrl + "/" + itemId + "/comment",
                new HttpEntity<>(commentDto, createHeadersWithUserId(userId)),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("должно быть хотя бы одно бронирование"));
    }

    private HttpHeaders createHeadersWithUserId(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}