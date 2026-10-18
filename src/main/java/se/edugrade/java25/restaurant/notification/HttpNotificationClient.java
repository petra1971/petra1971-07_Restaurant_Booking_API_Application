package se.edugrade.java25.restaurant.notification;

import org.springframework.stereotype.Component;

// Denna klass används inte nu. Sparas för framtiden när jag vill implementera en stubbad integration mot extern notificationklient (tror jag)
@Component
public class HttpNotificationClient implements NotificationClient {
    @Override
    public boolean sendConfirmation(Long bookingId) {
        return true;
    }
}
