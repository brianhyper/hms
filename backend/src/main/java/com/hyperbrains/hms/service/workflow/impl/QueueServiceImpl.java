package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.service.dto.view.VisitQueueItemDTO;
import com.hyperbrains.hms.service.rules.QueueKind;
import com.hyperbrains.hms.service.workflow.QueueService;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class QueueServiceImpl implements QueueService {

    private final VisitRepository visitRepository;

    public QueueServiceImpl(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Override
    public Page<VisitQueueItemDTO> page(QueueKind kind, Pageable pageable) {
        Page<Visit> visits = visitRepository.findQueue(kind.statuses(), pageable);

        // "Does selecting this row skip someone?" is a property of the row's position, not of the
        // row, so the counter has to carry the page offset rather than restart at zero. Reading on
        // page 2 must not tell staff the first patient on that page is next in line.
        AtomicInteger position = new AtomicInteger(Math.toIntExact(pageable.getOffset()));
        return visits.map(visit -> VisitQueueItemDTO.from(visit, position.getAndIncrement()));
    }

    @Override
    public Optional<VisitQueueItemDTO> head(QueueKind kind) {
        return visitRepository
            .findQueue(kind.statuses(), PageRequest.of(0, 1))
            .getContent()
            .stream()
            .findFirst()
            .map(visit -> VisitQueueItemDTO.from(visit, 0));
    }
}
