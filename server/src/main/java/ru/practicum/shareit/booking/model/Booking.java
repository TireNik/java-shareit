package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;


@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    Item item;

    @ManyToOne
    @JoinColumn(name = "booker_id", nullable = false)
    User booker;

    @Column(name = "start_time", nullable = false)
    LocalDateTime start;

    @Column(name = "end_time", nullable = false)
    LocalDateTime end;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    BookingStatus status;

}