package com.ticketapp.service;

import com.ticketapp.dto.PurchaseRequest;
import com.ticketapp.entity.Event;
import com.ticketapp.entity.Ticket;
import com.ticketapp.entity.User;               // ✅ YENİ
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.TicketRepository;
import com.ticketapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    TicketRepository ticketRepository;

    @Mock
    EventRepository eventRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    TicketService ticketService;

    @Test
    void purchase_whenCapacityEnough_savesTicketAndReturns() {
        // GIVEN
        PurchaseRequest request = new PurchaseRequest();
        request.eventId = 1L;
        request.quantity = 2;

        // ✅ Kullanıcıyı da mock'la
        User user = new User();
        user.setId(1L);
        user.setUsername("ahmet123");
        user.setPassword("secret");         // ne olduğunun önemi yok
        when(userRepository.findByUsername("ahmet123"))
                .thenReturn(Optional.of(user));

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Konser");
        event.setCity("Ankara");
        event.setType("music");
        event.setVenue("Arena");
        event.setDateTime(LocalDateTime.of(2025, 12, 10, 20, 0));
        event.setTotalSeats(100);
        event.setPrice(BigDecimal.valueOf(200));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ticketRepository.sumQuantityByEventId(1L)).thenReturn(10);

        // Kaydedilen ticket'a id verelim
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });

        // WHEN
        Ticket result = ticketService.purchase(request, "ahmet123");

        // THEN
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, times(1)).save(captor.capture());
        Ticket saved = captor.getValue();

        assertEquals(event, saved.getEvent());
        assertEquals("ahmet123", saved.getUsername());
        assertEquals(2, saved.getQuantity());
        assertNotNull(saved.getCreatedAt());
        assertEquals(100L, result.getId());
    }

    @Test
    void purchase_whenCapacityNotEnough_throwsExceptionAndDoesNotSave() {
        // GIVEN
        PurchaseRequest request = new PurchaseRequest();
        request.eventId = 1L;
        request.quantity = 5;

        User user = new User();
        user.setId(1L);
        user.setUsername("ahmet123");
        user.setPassword("secret");
        when(userRepository.findByUsername("ahmet123"))
                .thenReturn(Optional.of(user));

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Konser");
        event.setTotalSeats(10);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ticketRepository.sumQuantityByEventId(1L)).thenReturn(9);

        // WHEN
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ticketService.purchase(request, "ahmet123"));

        // THEN
        assertTrue(ex.getMessage().startsWith("Yeterli koltuk yok"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

}
