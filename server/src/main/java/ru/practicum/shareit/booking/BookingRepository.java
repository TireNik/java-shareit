package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerId(Long userId, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long userId, BookingStatus status, Sort sort);

    List<Booking> findByItemOwnerId(Long userId, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long userId, BookingStatus status, Sort sort);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime currentDateTime);

    Optional<Booking> findTopByItemIdAndEndBeforeAndStatusOrderByEndDesc(Long itemId, LocalDateTime end, BookingStatus status);

    Optional<Booking> findTopByItemIdAndStartBeforeAndEndAfterOrderByStartAsc(Long itemId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds AND b.status = 'APPROVED' ORDER BY b.start ASC")
    List<Booking> findAllByItemIdInAndStatusApproved(@Param("itemIds") List<Long> itemIds);
}