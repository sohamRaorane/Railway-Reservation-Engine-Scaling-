package com.soham.railway_reservation_engine.booking.validator;

import com.soham.railway_reservation_engine.bookings.dto.PassengerRequest;
import com.soham.railway_reservation_engine.bookings.validator.QuotaEligibilityValidator;
import com.soham.railway_reservation_engine.common.enums.BerthPreference;
import com.soham.railway_reservation_engine.common.enums.Gender;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QuotaEligibilityValidatorTest {
    private QuotaEligibilityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new QuotaEligibilityValidator();
    }
    //Do not allow the young males under the senior citizens quota
    @Test
    void shouldThrowExceptionForYoungMaleSeniorCitizen() {
        PassengerRequest passenger = new PassengerRequest(
                "Rahul",
                25,
                Gender.MALE,
                BerthPreference.LOWER
        );
        User user = new User();
        Quota quota = Quota.builder()
                .code("SS")
                .build();
        //So this test will throw a exception
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> validator.validate(passenger, user, quota)
        );
        // Assert that the exception message is as expected
        assertEquals(
                "Passenger is not eligible for Senior Citizen quota.",
                exception.getMessage()
        );
    }
    //Allow the senior citizens in the senior citizens quota
    @Test
    void shouldAllowSeniorMalePassenger() {
        PassengerRequest passenger = new PassengerRequest(
                "Rahul",
                65,
                Gender.MALE,
                BerthPreference.LOWER
        );
        User user = new User();
        Quota quota = Quota.builder()
                .code("SS")
                .build();
        assertDoesNotThrow(
                () -> validator.validate(passenger, user, quota)
        );
    }
    //Should allow the females for the female quota
    @Test
    void shouldAllowFemaleForLadiesQuota() {
        PassengerRequest passenger = new PassengerRequest(
                "Priya",
                24,
                Gender.FEMALE,
                BerthPreference.LOWER
        );
        User user = new User();
        Quota quota = Quota.builder()
                .code("LD")
                .build();
        assertDoesNotThrow(
                () -> validator.validate(passenger, user, quota)
        );
    }
    @Test
    void shouldRejectMaleForLadiesQuota() {
        PassengerRequest passenger = new PassengerRequest(
                "Rahul",
                24,
                Gender.MALE,
                BerthPreference.LOWER
        );
        User user = new User();
        Quota quota = Quota.builder()
                .code("LD")
                .build();
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> validator.validate(passenger, user, quota)
        );
        assertEquals(
                "Passenger is not eligible for Ladies Quota",
                exception.getMessage()
        );
    }
    @Test
    void shouldAllowDefenceUser() {
        PassengerRequest passenger = new PassengerRequest(
                "Rahul",
                25,
                Gender.MALE,
                BerthPreference.LOWER
        );
        User user = new User();
        user.setDefencePersonnel(true);
        Quota quota = Quota.builder()
                .code("DEF")
                .build();
        assertDoesNotThrow(
                () -> validator.validate(passenger, user, quota)
        );
    }
    @Test
    void shouldRejectNonDefenceUser() {
        PassengerRequest passenger = new PassengerRequest(
                "Rahul",
                25,
                Gender.MALE,
                BerthPreference.LOWER
        );
        User user = new User();
        user.setDefencePersonnel(false);
        Quota quota = Quota.builder()
                .code("DEF")
                .build();
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> validator.validate(passenger, user, quota)
        );
        assertEquals(
                "User is not eligible for Defence Quota.",
                exception.getMessage()
        );
    }
    @Test
    void shouldAllowGeneralQuotaForEveryone() {

        PassengerRequest passenger = new PassengerRequest(
                "Rahul",
                22,
                Gender.MALE,
                BerthPreference.LOWER
        );
        User user = new User();
        Quota quota = Quota.builder()
                .code("GN")
                .build();
        assertDoesNotThrow(
                () -> validator.validate(passenger, user, quota)
        );
    }

    @Test
    void shouldAllowTatkalQuotaForEveryone() {
        PassengerRequest passenger = new PassengerRequest(
                "Rahul",
                22,
                Gender.MALE,
                BerthPreference.LOWER
        );
        User user = new User();
        Quota quota = Quota.builder()
                .code("TQ")
                .build();
        assertDoesNotThrow(
                () -> validator.validate(passenger, user, quota)
        );
    }

}
