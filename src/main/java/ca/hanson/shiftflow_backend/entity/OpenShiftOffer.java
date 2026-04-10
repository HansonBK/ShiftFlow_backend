package ca.hanson.shiftflow_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "open_shift_offers")
public class OpenShiftOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "claimed_by_id")
    private User claimedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpenShiftStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() { this.createdAt = LocalDateTime.now(); this.updatedAt = LocalDateTime.now(); }

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Shift getShift() { return shift; }
    public User getCreatedBy() { return createdBy; }
    public User getClaimedBy() { return claimedBy; }
    public OpenShiftStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setShift(Shift shift) { this.shift = shift; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public void setClaimedBy(User claimedBy) { this.claimedBy = claimedBy; }
    public void setStatus(OpenShiftStatus status) { this.status = status; }
}