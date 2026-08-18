package com.soham.railway_reservation_engine.booking;


import com.soham.railway_reservation_engine.bookings.dto.BookingRequest;
import com.soham.railway_reservation_engine.bookings.dto.BookingResponse;
import com.soham.railway_reservation_engine.bookings.dto.PassengerRequest;
import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.bookings.service.BookingService;
import com.soham.railway_reservation_engine.coach.entity.Coach;
import com.soham.railway_reservation_engine.coach.repository.CoachRepository;
import com.soham.railway_reservation_engine.common.enums.*;
import com.soham.railway_reservation_engine.kafka.producer.BookingEventProducer;
import com.soham.railway_reservation_engine.quotaReservationPool.entity.QuotaReservationPool;
import com.soham.railway_reservation_engine.quotaReservationPool.repository.QuotaReservationPoolRepository;
import com.soham.railway_reservation_engine.quotaSeatAllocation.entity.QuotaSeatAllocation;
import com.soham.railway_reservation_engine.quotaSeatAllocation.repository.QuotaSeatAllocationRepository;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.schedule.repository.ScheduleRepository;
import com.soham.railway_reservation_engine.seat.entity.Seat;
import com.soham.railway_reservation_engine.seat.repository.SeatRepository;
import com.soham.railway_reservation_engine.train.entity.Train;
import com.soham.railway_reservation_engine.train.repository.TrainRepository;
import com.soham.railway_reservation_engine.user.entity.User;
import com.soham.railway_reservation_engine.user.repository.UserRepository;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.quota.repository.QuotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@SpringBootTest //integration test --> start the entire spring boot application context for this test
public class BookingFlowIntegrationTest {
    //fixing the local time issue of the postgres time mismatching
    static {
        TimeZone.setDefault(
                TimeZone.getTimeZone("Asia/Kolkata")
        );
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("railway_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7")
            .withExposedPorts(6379);

    @DynamicPropertySource
    //tells the spring that i am going to give you the connection properties dynamically
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );

        registry.add(
                "spring.data.redis.host",
                redis::getHost
        );
        //it tells us  which port currently maps to redis container port 6379
        registry.add(
                "spring.data.redis.port",
                () -> redis.getMappedPort(6379)
        );

        registry.add(
                "spring.flyway.enabled",
                () -> true
        );
    }
    //autowiring all the required dependencies for the test class
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TrainRepository trainRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private QuotaRepository quotaRepository;
    @Autowired
    private CoachRepository coachRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private QuotaSeatAllocationRepository quotaSeatAllocationRepository;
    @Autowired
    private QuotaReservationPoolRepository quotaReservationPoolRepository;
    @Autowired
    private BookingService bookingService;

    private User testUser;
    private Train testTrain;
    private Schedule testSchedule;
    private Quota testQuota;
    private Coach testCoach;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(
                User.builder()
                        .name("Test User")
                        .email("testuser@example.com")
                        .password("test-password")
                        .gender(Gender.MALE)
                        .dateOfBirth(LocalDate.of(2000, 1, 1))
                        .role(Role.USER)
                        .kycStatus(KycStatus.PENDING)
                        .defencePersonnel(false)
                        .build()
        );
        testTrain = trainRepository.save(
                Train.builder()
                        .number("12901")
                        .name("Test Express")
                        .type(TrainType.EXPRESS)
                        .build()
        );
        testSchedule = scheduleRepository.save(
                Schedule.builder()
                        .train(testTrain)
                        .journeyDate(LocalDate.now().plusDays(10))
                        .status(ScheduleStatus.OPEN)
                        .departureTime(LocalTime.of(10, 0))
                        .build()
        );

        testQuota = quotaRepository.save(
                Quota.builder()
                        .code("GN")
                        .name("General")
                        .build()
        );

        testCoach = coachRepository.save(
                Coach.builder()
                        .train(testTrain)
                        .coachNumber("S1")
                        .coachType(CoachType.SLEEPER)
                        .totalSeats(10)
                        .build()
        );

        for (int i = 1; i <= 10; i++) {

            Seat seat = Seat.builder()
                    .coach(testCoach)
                    .seatNumber(i)
                    .berthType(BerthType.LOWER)
                    .build();

            seatRepository.save(seat);
        }

        quotaSeatAllocationRepository.save(
                QuotaSeatAllocation.builder()
                        .schedule(testSchedule)
                        .quota(testQuota)
                        .coach(testCoach)
                        .totalSeats(10)
                        .availableSeats(10)
                        .build()
        );


        quotaReservationPoolRepository.save(
                QuotaReservationPool.builder()
                        .schedule(testSchedule)
                        .quota(testQuota)
                        .racLimit(5)
                        .racAvailable(5)
                        .waitlistLimit(10)
                        .waitlistAvailable(10)
                        .build()
        );
    }
    @Test
    void shouldCreateBookingSuccessfully() {

        List<PassengerRequest> passengers = List.of(
                new PassengerRequest(
                        "John Passenger",
                        25,
                        Gender.MALE,
                        BerthPreference.LOWER
                ),
                new PassengerRequest(
                        "Jane Passenger",
                        23,
                        Gender.FEMALE,
                        BerthPreference.MIDDLE
                )
        );

        BookingRequest bookingRequest = new BookingRequest(
                testTrain.getId(),
                LocalDate.now().plusDays(10),
                "GN",
                CoachType.SLEEPER,
                passengers
        );

        String idempotencyKey =
                "unique-key-" + System.currentTimeMillis();

        BookingResponse response = bookingService.bookTicket(
                testUser.getId(),
                bookingRequest,
                idempotencyKey
        );

        assertNotNull(response, "BookingResponse should not be null");

        assertNotNull(
                response.pnr(),
                "PNR should not be null"
        );

        assertEquals(
                BookingStatus.PENDING_PAYMENT,
                response.bookingStatus(),
                "Booking status should be PENDING_PAYMENT"
        );

        assertNotNull(
                response.totalFare(),
                "Total fare should not be null"
        );

        assertEquals(
                2,
                response.passengers().size(),
                "Should have 2 passengers"
        );

        assertEquals(
                "John Passenger",
                response.passengers().get(0).name(),
                "First passenger name should match"
        );

        assertEquals(
                "Jane Passenger",
                response.passengers().get(1).name(),
                "Second passenger name should match"
        );
    }

}
