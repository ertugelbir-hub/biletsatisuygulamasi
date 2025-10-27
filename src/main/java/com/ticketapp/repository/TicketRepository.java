package com.ticketapp.repository;

import com.ticketapp.entity.Ticket;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends org.springframework.data.jpa.repository.JpaRepository<Ticket, Long> {

    @Query("select coalesce(sum(t.quantity), 0) from Ticket t where t.event.id = :eventId")
    int sumQuantityByEventId(@Param("eventId") Long eventId);

    @Query("select t from Ticket t where t.username = :username")
    List<Ticket> findByUsername(@Param("username") String username);

}


