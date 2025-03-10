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
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class BookingIntegrationTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private String bookingBaseUrl;
    private String itemBaseUrl;
    private String userBaseUrl;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long userId;
    private Long ownerId;
    private Long itemId;
    private Long bookingId;

    @BeforeEach
    void setUp() {
        HttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplateBuilder builder = new RestTemplateBuilder().requestFactory(() -> factory);
        restTemplate = new TestRestTemplate(builder);

        bookingBaseUrl = "http://localhost:" + port + "/bookings";
        itemBaseUrl = "http://localhost:" + port + "/items";
        userBaseUrl = "http://localhost:" + port + "/users";

        jdbcTemplate.update("DELETE FROM bookings");
        jdbcTemplate.update("DELETE FROM items");
        jdbcTemplate.update("DELETE FROM users");

        UserDto ownerDto = new UserDto(null, "Pasha Technic", "pasha.technic@example.com");
        ResponseEntity<UserDto> ownerResponse = restTemplate.postForEntity(userBaseUrl, ownerDto, UserDto.class);
        assertEquals(HttpStatus.CREATED, ownerResponse.getStatusCode(), "Не удалось создать владельца: " + ownerResponse.getBody());
        ownerId = Objects.requireNonNull(ownerResponse.getBody()).getId();
        log.info("Создание владельца с ID: {}", ownerId);

        ItemDto itemDto = new ItemDto(null, "Item 1", "Description for Item 1", true, null, null, null, null);
        ResponseEntity<ItemDto> itemResponse = restTemplate.postForEntity(
                itemBaseUrl,
                new HttpEntity<>(itemDto, createHeadersWithUserId(ownerId)),
                ItemDto.class);
        assertEquals(HttpStatus.CREATED, itemResponse.getStatusCode(), "Не удалось создать вещь: " + itemResponse.getBody());
        itemId = Objects.requireNonNull(itemResponse.getBody()).getId();
        log.info("Создание вещи с ID: {}", itemId);

        UserDto bookerDto = new UserDto(null, "Alex Booker", "alex.booker@example.com");
        ResponseEntity<UserDto> bookerResponse = restTemplate.postForEntity(userBaseUrl, bookerDto, UserDto.class);
        assertEquals(HttpStatus.CREATED, bookerResponse.getStatusCode(), "Не удалось создать арендатора: " + bookerResponse.getBody());
        userId = Objects.requireNonNull(bookerResponse.getBody()).getId();
        log.info("Создание арендатора с ID: {}", userId);
    }

    @Test
    void createBooking_success() {
        BookingDto bookingDto = new BookingDto(itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        ResponseEntity<BookingDtoOut> response = restTemplate.postForEntity(
                bookingBaseUrl,
                new HttpEntity<>(bookingDto, createHeadersWithUserId(userId)),
                BookingDtoOut.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Не удалось создать бронирование: " + response.getBody());
        BookingDtoOut createdBooking = response.getBody();
        assertNotNull(createdBooking);
        assertNotNull(createdBooking.getId());
        assertEquals(itemId, createdBooking.getItem().getId());
        assertEquals(BookingStatus.WAITING, createdBooking.getStatus());
        log.info("Создано бронирование: {}", createdBooking);
    }

    @Test
    void confirmBooking_success() {
        BookingDto bookingDto = new BookingDto(itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        ResponseEntity<BookingDtoOut> bookingResponse = restTemplate.postForEntity(
                bookingBaseUrl,
                new HttpEntity<>(bookingDto, createHeadersWithUserId(userId)),
                BookingDtoOut.class);
        assertEquals(HttpStatus.CREATED, bookingResponse.getStatusCode(), "Не удалось создать бронирование: " + bookingResponse.getBody());
        bookingId = Objects.requireNonNull(bookingResponse.getBody()).getId();

        ResponseEntity<BookingDtoOut> confirmResponse = restTemplate.exchange(
                bookingBaseUrl + "/" + bookingId + "?approved=true",
                HttpMethod.PATCH,
                new HttpEntity<>(createHeadersWithUserId(ownerId)),
                BookingDtoOut.class);

        assertEquals(HttpStatus.OK, confirmResponse.getStatusCode(), "Не удалось подтвердить бронирование: " + confirmResponse.getBody());
        BookingDtoOut confirmedBooking = confirmResponse.getBody();
        assertNotNull(confirmedBooking);
        assertEquals(BookingStatus.APPROVED, confirmedBooking.getStatus());
        log.info("Бронирование подтверждено: {}", confirmedBooking);
    }

    @Test
    void getBooking_success() {
        BookingDto bookingDto = new BookingDto(itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        ResponseEntity<BookingDtoOut> bookingResponse = restTemplate.postForEntity(
                bookingBaseUrl,
                new HttpEntity<>(bookingDto, createHeadersWithUserId(userId)),
                BookingDtoOut.class);
        assertEquals(HttpStatus.CREATED, bookingResponse.getStatusCode(), "Не удалось создать бронирование: " + bookingResponse.getBody());
        bookingId = Objects.requireNonNull(bookingResponse.getBody()).getId();

        ResponseEntity<BookingDtoOut> response = restTemplate.exchange(
                bookingBaseUrl + "/" + bookingId,
                HttpMethod.GET,
                new HttpEntity<>(createHeadersWithUserId(userId)),
                BookingDtoOut.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Не удалось получить бронирование: " + response.getBody());
        BookingDtoOut retrievedBooking = response.getBody();
        assertNotNull(retrievedBooking);
        assertEquals(bookingId, retrievedBooking.getId());
        log.info("Получено бронирование: {}", retrievedBooking);
    }

    @Test
    void getAllBookings_success() {
        BookingDto bookingDto = new BookingDto(itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        ResponseEntity<BookingDtoOut> bookingResponse = restTemplate.postForEntity(
                bookingBaseUrl,
                new HttpEntity<>(bookingDto, createHeadersWithUserId(userId)),
                BookingDtoOut.class);
        assertEquals(HttpStatus.CREATED, bookingResponse.getStatusCode(), "Не удалось создать бронирование: " + bookingResponse.getBody());

        ResponseEntity<BookingDtoOut[]> response = restTemplate.exchange(
                bookingBaseUrl + "?state=ALL",
                HttpMethod.GET,
                new HttpEntity<>(createHeadersWithUserId(userId)),
                BookingDtoOut[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Не удалось получить все бронирования: " + Arrays.toString(response.getBody()));
        BookingDtoOut[] bookings = response.getBody();
        assertNotNull(bookings);
        assertEquals(1, bookings.length);
        log.info("Получены все бронирования арендатора: {}", Arrays.toString(bookings));
    }

    @Test
    void getAllBookingsForOwner_success() {
        BookingDto bookingDto = new BookingDto(itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        ResponseEntity<BookingDtoOut> bookingResponse = restTemplate.postForEntity(
                bookingBaseUrl,
                new HttpEntity<>(bookingDto, createHeadersWithUserId(userId)),
                BookingDtoOut.class);
        assertEquals(HttpStatus.CREATED, bookingResponse.getStatusCode(), "Не удалось создать бронирование: " + bookingResponse.getBody());

        ResponseEntity<BookingDtoOut[]> response = restTemplate.exchange(
                bookingBaseUrl + "/owner?state=ALL",
                HttpMethod.GET,
                new HttpEntity<>(createHeadersWithUserId(ownerId)),
                BookingDtoOut[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Не удалось получить все бронирования владельца: " + Arrays.toString(response.getBody()));
        BookingDtoOut[] bookings = response.getBody();
        assertNotNull(bookings);
        assertEquals(1, bookings.length);
        log.info("Получены все бронирования владельца: {}", Arrays.toString(bookings));
    }

    private HttpHeaders createHeadersWithUserId(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}