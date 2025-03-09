package ru;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = ru.practicum.shareit.ShareItApp.class)
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {

    private final ItemServiceImpl itemService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private User owner;
    private User booker;
    private Item item;


    @BeforeEach
    void setUp() {
        item = itemRepository.save(new Item(null, "Test Item", "Test Description", true, new User(1L, "dsaf", "dsf@asd.com"), null));
    }

    @Test
    void teHuest() {
        System.out.println(item);
    }

//    @Test
//    void getItemById_whenNoBookingsAndComments_returnsItemWithNullBookingsAndEmptyComments() {
//        // Act
//        ItemDto result = itemService.getItemById(item.getId());
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(item.getId());
//        assertThat(result.getName()).isEqualTo("Test Item");
//        assertThat(result.getDescription()).isEqualTo("Test Description");
//        assertThat(result.getAvailable()).isTrue();
//        assertThat(result.getLastBooking()).isNull();
//        assertThat(result.getNextBooking()).isNull();
//        assertThat(result.getComments()).isEmpty();
//    }
}