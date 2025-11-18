package com.ticketapp.service;

import com.ticketapp.dto.EventRequest;
import com.ticketapp.dto.SalesReport;
import com.ticketapp.entity.Event;
import com.ticketapp.exception.ErrorMessages;
import com.ticketapp.exception.ResourceNotFoundException;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventServiceTest {

    // Gerçek Spring context açmıyoruz; direkt mock + normal class
    private final EventRepository eventRepository = Mockito.mock(EventRepository.class);
    private final TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
    private final EventService eventService = new EventService(eventRepository, ticketRepository);

    /**
     * create(EventRequest) → Event oluşturup repo.save'e gönderiyor mu?
     */
    @Test
    void create_mapsFieldsAndSaves() {
        EventRequest r = new EventRequest();
        r.setTitle("Konser");
        r.setCity("Ankara");
        r.setType("music");
        r.setVenue("Arena");
        r.setDateTime(LocalDateTime.of(2025, 12, 10, 20, 0));
        r.setTotalSeats(100);
        r.setPrice(BigDecimal.valueOf(150));

        // repo.save çağrıldığında geri dönecek Event
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event e = invocation.getArgument(0);
            e.setId(1L); // kaydedilmiş gibi id verelim
            return e;
        });

        Event result = eventService.create(r);

        // repo.save'e giden Event'in alanlarını doğrula
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository, times(1)).save(captor.capture());
        Event saved = captor.getValue();

        assertEquals("Konser", saved.getTitle());
        assertEquals("Ankara", saved.getCity());
        assertEquals("music", saved.getType());
        assertEquals("Arena", saved.getVenue());
        assertEquals(LocalDateTime.of(2025, 12, 10, 20, 0), saved.getDateTime());
        assertEquals(100, saved.getTotalSeats());
        assertEquals(BigDecimal.valueOf(150), saved.getPrice());

        // dönen sonuçta id atanmış mı
        assertEquals(1L, result.getId());
    }

    /**
     * update(id, request) → event yoksa ResourceNotFoundException atmalı.
     */
    @Test
    void update_whenEventNotFound_throwsResourceNotFoundException() {
        when(eventRepository.findById(5L)).thenReturn(Optional.empty());

        EventRequest r = new EventRequest();
        r.setTitle("Yeni Başlık");

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> eventService.update(5L, r));

        assertEquals(ErrorMessages.EVENT_NOT_FOUND, ex.getMessage());
    }

    /**
     * delete(id) → event yoksa RuntimeException("Event bulunamadı") atmalı.
     */
    @Test
    void delete_whenEventDoesNotExist_throwsRuntimeException() {
        when(eventRepository.existsById(10L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.delete(10L));

        assertEquals(ErrorMessages.EVENT_NOT_FOUND, ex.getMessage());
        // deleteById hiç çağrılmamalı
        verify(eventRepository, never()).deleteById(anyLong());
    }

    /**
     * salesReport(eventId) → sold / remaining / revenue hesaplıyor mu?
     */
    @Test
    void salesReport_calculatesSoldRemainingAndRevenue() {
        Event e = new Event();
        e.setId(1L);
        e.setTitle("Konser");
        e.setCity("Ankara");
        e.setVenue("Arena");
        e.setDateTime(LocalDateTime.of(2025, 12, 10, 20, 0));
        e.setTotalSeats(100);
        e.setPrice(BigDecimal.valueOf(200));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(e));
        when(ticketRepository.sumQuantityByEventId(1L)).thenReturn(30); // 30 bilet satıldı

        SalesReport report = eventService.salesReport(1L);

        assertEquals(1L, report.eventId);
        assertEquals(30, report.sold);
        assertEquals(70, report.remaining);
        assertEquals(BigDecimal.valueOf(200), report.price);
        assertEquals(BigDecimal.valueOf(6000), report.revenue); // 30 * 200
    }

    /**
     * listPaged(Pageable) → repo.findAll(pageable)'a delege ediyor mu?
     */
    @Test
    void listPaged_delegatesToRepository() {
        Event e1 = new Event();
        e1.setId(1L);
        Page<Event> page = new PageImpl<>(List.of(e1), PageRequest.of(0, 10), 1);

        when(eventRepository.findAll(any(Pageable.class))).thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> result = eventService.listPaged(pageable);

        assertEquals(1, result.getTotalElements());
        verify(eventRepository, times(1)).findAll(pageable);
    }

    /**
     * search(...) → repo.search(...)'e parametreleri aynen geçiriyor mu?
     */
    @Test
    void search_delegatesToRepository() {
        Page<Event> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(eventRepository.search(anyString(), anyString(), anyString(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> result = eventService.search("Ankara", "music", "konser",
                null, null, pageable);

        assertNotNull(result);
        verify(eventRepository, times(1))
                .search(eq("Ankara"), eq("music"), eq("konser"),
                        isNull(), isNull(), eq(pageable));
    }
}
