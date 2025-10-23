package com.ticketapp.repository;

import com.ticketapp.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface    EventRepository extends JpaRepository<Event, Long> {}
