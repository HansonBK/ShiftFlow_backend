package ca.hanson.shiftflow_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "swap_requests")
public class SwapRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne(optional = false)
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requester_shift_id")
    private Shift requesterShift;

    @ManyToOne(optional = false)
    @JoinColumn(name = "target_shift_id")
    private Shift targetShift;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SwapRequestStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime respondedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}