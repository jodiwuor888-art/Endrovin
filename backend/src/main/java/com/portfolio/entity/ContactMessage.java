package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ContactMessages")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MessageID")
    private Long id;

    @Column(name = "FullName", nullable = false, length = 100)
    private String fullName;

    @Column(name = "Email", nullable = false, length = 120)
    private String email;

    @Column(name = "Subject", nullable = false, length = 200)
    private String subject;

    @Lob
    @Column(name = "Message", nullable = false)
    private String message;

    @Column(name = "IsRead", nullable = false)
    private boolean read = false;

    @Column(name = "Status", nullable = false, length = 20)
    private String status = "NEW"; // NEW, READ, ARCHIVED, DELETED

    @Column(name = "Phone", length = 50)
    private String phone;

    @Column(name = "Company", length = 150)
    private String company;

    @Column(name = "ServiceInterest", length = 100)
    private String serviceInterest;

    @Column(name = "EstimatedBudget", length = 100)
    private String estimatedBudget;

    @Column(name = "LeadStatus", nullable = false, length = 30)
    private String leadStatus = "NEW"; // NEW, CONTACTED, IN_DISCUSSION, QUOTATION_SENT, NEGOTIATING, WON, LOST

    @Column(name = "FollowUpDate")
    private LocalDateTime followUpDate;

    @Lob
    @Column(name = "InternalNotes", columnDefinition = "LONGTEXT")
    private String internalNotes;

    @Lob
    @Column(name = "QuotationDetails", columnDefinition = "LONGTEXT")
    private String quotationDetails;

    @Column(name = "ClientIp", length = 50)
    private String clientIp;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "ReadAt")
    private LocalDateTime readAt;

    public ContactMessage() {
    }

    public ContactMessage(String fullName, String email, String subject, String message, String clientIp) {
        this.fullName = fullName;
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.clientIp = clientIp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getServiceInterest() {
        return serviceInterest;
    }

    public void setServiceInterest(String serviceInterest) {
        this.serviceInterest = serviceInterest;
    }

    public String getEstimatedBudget() {
        return estimatedBudget;
    }

    public void setEstimatedBudget(String estimatedBudget) {
        this.estimatedBudget = estimatedBudget;
    }

    public String getLeadStatus() {
        return leadStatus;
    }

    public void setLeadStatus(String leadStatus) {
        this.leadStatus = leadStatus;
    }

    public LocalDateTime getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(LocalDateTime followUpDate) {
        this.followUpDate = followUpDate;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    public String getQuotationDetails() {
        return quotationDetails;
    }

    public void setQuotationDetails(String quotationDetails) {
        this.quotationDetails = quotationDetails;
    }
}
