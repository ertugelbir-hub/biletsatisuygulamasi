package com.ticketapp.repository;

import com.ticketapp.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface EventRepository extends org.springframework.data.jpa.repository.JpaRepository<Event, Long> {
    @Query("""
      select e from Event e
where (:city is null or lower(e.city) = lower(:city))
  and (:type is null or lower(e.type) = lower(:type))
  and (:q    is null or lower(e.title) like lower(concat('%', :q, '%')))
  and (:from is null or e.dateTime >= :from)
  and (:to   is null or e.dateTime <= :to)
""")
    Page<Event> search(
            @Param("city") String city,
            @Param("type") String type,
            @Param("q") String q,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );


}
