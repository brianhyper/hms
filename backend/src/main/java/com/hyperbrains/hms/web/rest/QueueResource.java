package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.VisitQueueItemDTO;
import com.hyperbrains.hms.service.rules.QueueKind;
import com.hyperbrains.hms.service.workflow.QueueService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

/**
 * Read-only work queues.
 *
 * <p>Each row carries {@code requiresSkipReason} when selecting it would skip past a patient who
 * ranks ahead of it. Selection is never blocked by rank — staff have reasons the system cannot see
 * — but the reason has to be recorded, and this is where the client learns that.
 */
@RestController
@RequestMapping("/api/queues")
public class QueueResource {

    private static final Logger LOG = LoggerFactory.getLogger(QueueResource.class);

    private final QueueService queueService;

    public QueueResource(QueueService queueService) {
        this.queueService = queueService;
    }

    /** {@code GET /queues/vitals} : patients waiting to be triaged. */
    @GetMapping("/vitals")
    public ResponseEntity<List<VisitQueueItemDTO>> vitalsQueue(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get the vitals queue");
        return page(QueueKind.VITALS, pageable);
    }

    /** {@code GET /queues/consultation} : triaged patients waiting for a doctor. */
    @GetMapping("/consultation")
    public ResponseEntity<List<VisitQueueItemDTO>> consultationQueue(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get the consultation queue");
        return page(QueueKind.CONSULTATION, pageable);
    }

    /** {@code GET /queues/active} : every visit still in progress, for supervision. */
    @GetMapping("/active")
    public ResponseEntity<List<VisitQueueItemDTO>> activeVisits(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get active visits");
        return page(QueueKind.ACTIVE, pageable);
    }

    private ResponseEntity<List<VisitQueueItemDTO>> page(QueueKind kind, Pageable pageable) {
        Page<VisitQueueItemDTO> page = queueService.page(kind, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
