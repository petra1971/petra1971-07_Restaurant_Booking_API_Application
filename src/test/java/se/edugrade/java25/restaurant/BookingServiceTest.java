package se.edugrade.java25.restaurant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.edugrade.java25.restaurant.dto.CreateBookingRequest;
import se.edugrade.java25.restaurant.model.Booking;
import se.edugrade.java25.restaurant.model.Restaurant;
import se.edugrade.java25.restaurant.notification.NotificationClient;
import se.edugrade.java25.restaurant.repository.BookingRepository;
import se.edugrade.java25.restaurant.repository.RestaurantRepository;
import se.edugrade.java25.restaurant.service.BookingService;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

// Jag  använder Mockito eftersom jag inte har någon riktig HttpNotificationClient att simulera anrop ifrån
// Jag vill bara kontrollera att BookingService anropar NotificationClient när en bokning bekräftas.
// Hade jag haft en riktig HttpNotificationClient jag ville simulera skulle jag istället valt WireMock.
@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void confirm_shouldSendNotification() {

        // Arrange
        Restaurant restaurant = mock(Restaurant.class);
        Booking booking = new Booking(
                "Anna Andersson",
                "anna@example.com",
                LocalDateTime.now().plusDays(1),
                2,
                restaurant);

        Mockito.when(notificationClient.sendConfirmation(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // Act
        bookingService.confirm(1L);

        // Assert
        verify(notificationClient).sendConfirmation(1L);
        assertTrue(booking.isConfirmationSent());
    }

    @Test // låter NotificationClient kasta NotificationException och verifierar att bokningen ändå bekräftas.
    void confirm_shouldSucceedWhenNotificationFails() {

        // Arrange
        Restaurant restaurant = mock(Restaurant.class);
        Booking booking = new Booking(
                "Anna Andersson",
                "anna@example.com",
                LocalDateTime.now().plusDays(1),
                2,
                restaurant);

        Mockito.when(notificationClient.sendConfirmation(1L))
                .thenReturn(false);
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // Act
        bookingService.confirm(1L);

        // Assert
        assertFalse(booking.isConfirmationSent());
    }
}
