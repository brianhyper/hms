package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.VisitQueueItemDTO;
import com.hyperbrains.hms.service.rules.QueueKind;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Read access to the work queues.
 *
 * <p>Kept separate from the services that move patients through the workflow, so that showing a
 * queue cannot accidentally change one.
 */
public interface QueueService {

    /** A page of a queue, ordered the way staff work through it. */
    Page<VisitQueueItemDTO> page(QueueKind kind, Pageable pageable);

    /**
     * The visit currently at the head of a queue, if any.
     *
     * <p>Used to decide whether a selection is out of order. It reads through the same ordering as
     * {@link #page}, so the row the screen shows at the top and the row this considers next can
     * never diverge.
     */
    Optional<VisitQueueItemDTO> head(QueueKind kind);
}
