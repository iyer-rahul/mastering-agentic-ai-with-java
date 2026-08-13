package com.telusko.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import com.telusko.enums.TicketCategory;
import com.telusko.enums.TicketPriority;
import com.telusko.enums.TicketStatus;

@Entity
@Table(name = "support_tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupportTicket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Column(nullable = false)
    private String subject;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.OPEN;

    // ---- Filled in by AI triage when the ticket is created ----
    // Nullable on purpose: triage is best-effort, and a ticket must still be created if the
    // model is unavailable.
    @Enumerated(EnumType.STRING)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    private TicketPriority priority;

    /** A one-line restatement of the problem, so admins can scan the queue without opening each ticket. */
    private String aiSummary;

    /**
     * A draft the admin can edit and send, rather than starting from a blank box.
     * <p>
     * Hidden from JSON: the customer's own ticket endpoints serialise this entity directly, and an
     * internal draft reply is not something the customer should be reading. Admins get it through
     * {@link com.telusko.dto.TicketView}.
     */
    @JsonIgnore
    @Lob
    private String suggestedReply;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id")
    private Order order;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String assignedToEmail;
}