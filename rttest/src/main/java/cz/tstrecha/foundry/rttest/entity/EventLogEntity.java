package cz.tstrecha.foundry.rttest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "event_log")
@NoArgsConstructor
@AllArgsConstructor
public class EventLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "message")
    private String message;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "occurrence_no")
    private Long occurrenceNo;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_no")
    private Long auditNo;
}
