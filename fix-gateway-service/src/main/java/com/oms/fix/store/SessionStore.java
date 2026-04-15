package com.oms.fix.store;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import quickfix.SessionID;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionStore {
    private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();

    public void registerSession(SessionID sessionID) {
        activeSessions.add(sessionID.toString());
        log.info("Session registered: {}", sessionID);
    }

    public void deregisterSession(SessionID sessionID) {
        activeSessions.remove(sessionID.toString());
        log.info("Session deregistered: {}", sessionID);
    }

    public boolean isActive(SessionID sessionID) {
        return activeSessions.contains(sessionID.toString());
    }

    public int activeCount() {
        return activeSessions.size();
    }
}
