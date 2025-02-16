package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId, Sort sort);

    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status, Sort sort);

    List<Booking> findByItemOwnerId(Long userId, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long userId, BookingStatus status, Sort sort);

    boolean existsByItemIdAndUserIdAndEndBefore(Long itemId, Long userId, LocalDateTime now);

    Optional<Booking> findTopByItemIdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime now);

    Optional<Booking> findTopByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime now);
}