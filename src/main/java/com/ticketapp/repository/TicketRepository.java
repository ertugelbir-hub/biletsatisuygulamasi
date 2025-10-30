package com.ticketapp.repository;

import com.ticketapp.dto.SalesSummaryItem;
import com.ticketapp.entity.Ticket;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends org.springframework.data.jpa.repository.JpaRepository<Ticket, Long> {

    // Tüm zamanlar için toplam satılan adet (remaining hesaplamakta kullanacağız)
    @Query("select coalesce(sum(t.quantity), 0) from Ticket t where t.event.id = :eventId")
    int sumQuantityByEventId(@Param("eventId") Long eventId);

    // Belirli tarih aralığında satılan adet (rapordaki 'sold')
    @Query("""
           select coalesce(sum(t.quantity), 0)
           from Ticket t
           where t.event.id = :eventId
             and t.createdAt between :from and :to
           """)
    int sumQuantityByEventIdBetween(@Param("eventId") Long eventId,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);

    @Query("select t from Ticket t where t.username = :username")
    List<Ticket> findByUsername(@Param("username") String username);
    // ✅ ÖZET RAPOR: tarih aralığına göre tüm event’ler için (tek sorgu)
    @Query("""
           select new com.ticketapp.dto.SalesSummaryItem(
               e.id,
               e.title,
               e.totalSeats,
               coalesce(sum(ts.quantity), 0),
               ( e.totalSeats - coalesce(
                     (select sum(t2.quantity) from Ticket t2 where t2.event.id = e.id), 0
                 )
               ),
               e.price,
               null
           )
           from Event e
           left join Ticket ts
             on ts.event.id = e.id
            and ts.createdAt between :from and :to
           group by e.id, e.title, e.totalSeats, e.price
           order by e.dateTime
           """)
    List<SalesSummaryItem> summaryByEventDate(@Param("from") LocalDateTime from,
                                              @Param("to")   LocalDateTime to);
}




