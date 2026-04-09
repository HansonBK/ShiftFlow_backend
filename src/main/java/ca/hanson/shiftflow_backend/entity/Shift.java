package ca.hanson.shiftflow_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;


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

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getPosition() {
        return position;
    }

    public String getLocation() {
        return location;
    }

    public User getAssignedEmployee() {
        return assignedEmployee;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setAssignedEmployee(User assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
