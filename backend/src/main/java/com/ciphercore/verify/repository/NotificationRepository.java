package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientUsernameOrderByCreatedAtDesc(String recipientUsername);

    List<Notification> findTop15ByRecipientUsernameOrderByCreatedAtDesc(String recipientUsername);

    long countByRecipientUsernameAndIsReadFalse(String recipientUsername);

    List<Notification> findByIsReadFalseOrderByCreatedAtDesc();
}
