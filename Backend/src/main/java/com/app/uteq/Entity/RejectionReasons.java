package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "rejectionreasons")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RejectionReasons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idrejectionreason")
    private Integer idRejectionReason;

    @Column(name = "reasoncode", nullable = false, length = 50, unique = true)
    private String reasonCode;

    @Column(name = "reasondescription", nullable = false)
    private String reasonDescription;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "createdat")
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;
}
