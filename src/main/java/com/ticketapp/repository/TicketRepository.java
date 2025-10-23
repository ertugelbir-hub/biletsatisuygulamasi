package com.ticketapp.repository;

import com.ticketapp.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("select coalesce(sum(t.quantity), 0) from Ticket t where t.event.id = :eventId")
    int sumQuantityByEventId(@Param("eventId") Long eventId);
}


