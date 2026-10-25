package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.Notification;
import com.ciphercore.verify.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Deliverable 6: Notification Controller
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @RequestHeader(value = "X-Username", defaultValue = "officer_sc401") String username) {
        return ResponseEntity.ok(notificationRepository.findTop15ByRecipientUsernameOrderByCreatedAtDesc(username));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader(value = "X-Username", defaultValue = "officer_sc401") String username) {
        long count = notificationRepository.countByRecipientUsernameAndIsReadFalse(username);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        return notificationRepository.findById(id).map(notif -> {
            notif.setIsRead(true);
            notif.setReadAt(LocalDateTime.now());
            notificationRepository.save(notif);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            @RequestHeader(value = "X-Username", defaultValue = "officer_sc401") String username) {
        List<Notification> unread = notificationRepository.findByRecipientUsernameOrderByCreatedAtDesc(username);
        for (Notification n : unread) {
            if (!Boolean.TRUE.equals(n.getIsRead())) {
                n.setIsRead(true);
                n.setReadAt(LocalDateTime.now());
                notificationRepository.save(n);
            }
        }
        return ResponseEntity.ok(Map.of("success", true, "count", unread.size()));
    }
}
