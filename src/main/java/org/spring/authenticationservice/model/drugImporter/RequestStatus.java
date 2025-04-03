package org.spring.authenticationservice.model.drugImporter;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.spring.authenticationservice.model.Enum.RequestStatusEnum;

@Entity
@Table(name = "request_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "drugimporter_id", nullable = false)
    private Long drugImporterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatusEnum status;
}
