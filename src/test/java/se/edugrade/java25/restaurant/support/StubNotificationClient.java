package se.edugrade.java25.restaurant.support;

import org.springframework.stereotype.Component;
import se.edugrade.java25.restaurant.notification.NotificationClient;

// Denna klass används inte, men jag sparar den för användning när jag lägger till test där jag behöver stubba externa tjänsten
public final class StubNotificationClient implements NotificationClient {

    @Override
    public boolean sendConfirmation(Long bookingId) {
        return true;
    }
}

