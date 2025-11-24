package com.ticketapp.repository;

import com.ticketapp.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // BU IMPORT ÇOK ÖNEMLİ
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    // JpaSpecificationExecutor sayesinde 'findAll(Specification, Pageable)' metodu otomatik gelir.
    // Ekstra kod yazmana gerek yok.
}