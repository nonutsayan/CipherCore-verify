package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.ActionItem;
import com.ciphercore.verify.model.enums.ActionItemPriority;
import com.ciphercore.verify.model.enums.ActionItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {

    List<ActionItem> findByApplicationId(Long applicationId);

    List<ActionItem> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);

    List<ActionItem> findByStatusNotOrderByPriorityDescCreatedAtAsc(ActionItemStatus status);

    List<ActionItem> findByAssignedToAndStatusNot(String assignedTo, ActionItemStatus status);

    Page<ActionItem> findByStatus(ActionItemStatus status, Pageable pageable);

    long countByStatus(ActionItemStatus status);

    long countByPriorityAndStatusNot(ActionItemPriority priority, ActionItemStatus status);

    List<ActionItem> findTop10ByStatusNotOrderByPriorityDescCreatedAtAsc(ActionItemStatus status);
}
