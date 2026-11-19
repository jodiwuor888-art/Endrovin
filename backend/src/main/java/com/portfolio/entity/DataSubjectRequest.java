package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_subject_requests")
public class DataSubjectRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 50)
    private String ticketNumber;

    @Column(name = "requester_name", nullable = false, length = 100)
    private String requesterName;

    @Column(name = "requester_email", nullable = false, length = 120)
    private String requesterEmail;

    @Column(name = "request_type", nullable = false, length = 50)
    private String requestType; // "ACCESS", "CORRECTION", "OBJECTION", "DELETION", "WITHDRAWAL", "COMPLAINT"

    @Lob
    @Column(name = "details", nullable = false, columnDefinition = "LONGTEXT")
    private String details;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING"; // "PENDING", "UNDER_REVIEW", "COMPLETED", "REJECTED"

    @Lob
    @Column(name = "admin_notes", columnDefinition = "LONGTEXT")
    private String adminNotes;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "deadline_date")
    private LocalDateTime deadlineDate;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public DataSubjectRequest() {
    }

    public DataSubjectRequest(String ticketNumber, String requesterName, String requesterEmail, String requestType, String details, String clientIp) {
        this.ticketNumber = ticketNumber;
        this.requesterName = requesterName;
        this.requesterEmail = requesterEmail;
        this.requestType = requestType;
        this.details = details;
        this.clientIp = clientIp;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.deadlineDate = LocalDateTime.now().plusDays(30); // 30 days statutory turnaround under standard data protection laws
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(LocalDateTime deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
