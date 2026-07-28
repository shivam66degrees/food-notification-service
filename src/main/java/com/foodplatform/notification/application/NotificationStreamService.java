package com.foodplatform.notification.application;

import com.foodplatform.notification.infrastructure.persistence.NotificationJpaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationStreamService {

    private static final Logger log = LoggerFactory.getLogger(NotificationStreamService.class);

    private final long emitterTimeoutMs;
    private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> subscribers = new ConcurrentHashMap<>();

    public NotificationStreamService(
            @Value("${notification.stream.timeout-ms}") long emitterTimeoutMs
    ) {
        this.emitterTimeoutMs = emitterTimeoutMs;
    }

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(emitterTimeoutMs);
        subscribers.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable cleanup = () -> removeEmitter(userId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());

        sendConnectedEvent(userId, emitter);
        log.debug("SSE subscriber connected userId={}", userId);
        return emitter;
    }

    public void publish(UUID recipientUserId, NotificationStreamPayload payload) {
        List<SseEmitter> emitters = subscribers.get(recipientUserId);
        if (emitters == null || emitters.isEmpty()) {
            log.debug("No SSE subscribers for userId={}", recipientUserId);
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(payload));
            } catch (IOException ex) {
                log.debug("Removing failed SSE emitter for userId={}", recipientUserId);
                removeEmitter(recipientUserId, emitter);
            }
        }
    }

    @Scheduled(fixedDelayString = "${notification.stream.heartbeat-interval-ms}")
    void sendHeartbeats() {
        subscribers.forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                } catch (IOException ex) {
                    removeEmitter(userId, emitter);
                }
            }
        });
    }

    static NotificationStreamPayload fromEntity(NotificationJpaEntity entity) {
        return new NotificationStreamPayload(
                entity.getId(),
                entity.getOrderId(),
                entity.getEventType(),
                entity.getTitle(),
                entity.getBody(),
                entity.getCreatedAt()
        );
    }

    private void sendConnectedEvent(UUID userId, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"status\":\"connected\"}"));
        } catch (IOException ex) {
            log.debug("Failed to send connected event for userId={}", userId);
            removeEmitter(userId, emitter);
        }
    }

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = subscribers.get(userId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            subscribers.remove(userId, emitters);
        }
    }
}
