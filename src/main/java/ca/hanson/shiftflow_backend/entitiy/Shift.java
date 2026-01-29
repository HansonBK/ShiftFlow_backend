package ca.hanson.shiftflow_backend.entitiy;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="shifts")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private LocalDateTime startTime;
    @Column(nullable=false)
    private LocalDateTime endTime;

    private String position;
    private String location;

    @ManyToOne
    @JoinColumn(name="assigned_employee_id")
    private User assignedEmployee;

    @ManyToOne
    @JoinColumn(name="created_by_manager_id")
    private User createdBy;

    @Column(nullable=false)
    private LocalDateTime createdAt;


    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
