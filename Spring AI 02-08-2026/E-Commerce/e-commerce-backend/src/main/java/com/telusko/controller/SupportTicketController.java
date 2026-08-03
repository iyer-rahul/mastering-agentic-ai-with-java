package com.telusko.controller;

import com.telusko.dto.CreateTicketRequest;
import com.telusko.dto.UpdateTicketStatusRequest;
import com.telusko.model.SupportMessage;
import com.telusko.model.SupportTicket;
import com.telusko.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/v1/ecommerce/support")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService service;

    @PostMapping("/tickets")
    public ResponseEntity<SupportTicket> createTicket(
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody CreateTicketRequest request
    ) {
        SupportTicket ticket = service.createTicket(currentUser.getUsername(), request);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/tickets")
    public ResponseEntity<Page<SupportTicket>> getMyTickets(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                service.getMyTickets(currentUser.getUsername(), page, limit)
        );
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<SupportTicket> getMyTicketById(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                service.getMyTicketById(currentUser.getUsername(), id)
        );
    }

    // message body is just plain text:  "hello support..."
    @PostMapping("/tickets/{id}/messages")
    public ResponseEntity<SupportMessage> addUserMessage(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long id,
            @RequestBody String message
    ) {
        SupportMessage msg =
                service.addUserMessage(currentUser.getUsername(), id, message);
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/tickets/{id}/messages")
    public ResponseEntity<List<SupportMessage>> getMyMessages(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                service.getMyTicketMessages(currentUser.getUsername(), id)
        );
    }

    // -------- ADMIN --------

    @GetMapping("/admin/tickets")
    public ResponseEntity<Page<SupportTicket>> getAllTickets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(service.getAllTickets(page, limit, status));
    }

    @GetMapping("/admin/tickets/{id}")
    public ResponseEntity<SupportTicket> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTicketById(id));
    }

    // message body is plain text too
    @PostMapping("/admin/tickets/{id}/messages")
    public ResponseEntity<SupportMessage> addAdminMessage(
            @PathVariable Long id,
            @RequestBody String message
    ) {
        return ResponseEntity.ok(service.addAdminMessage(id, message));
    }

    @PatchMapping("/admin/tickets/{id}/status")
    public ResponseEntity<SupportTicket> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateTicketStatusRequest request
    ) {
        return ResponseEntity.ok(service.updateStatus(id, request));
    }

    @GetMapping("/admin/tickets/{id}/messages")
    public ResponseEntity<List<SupportMessage>> getMessagesAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTicketMessagesAdmin(id));
    }
}
