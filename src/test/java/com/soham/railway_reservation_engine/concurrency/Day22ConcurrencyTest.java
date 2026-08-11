package com.soham.railway_reservation_engine.concurrency;

import com.soham.railway_reservation_engine.bookings.entity.Booking;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day22ConcurrencyTest {
    private static final int TOTAL_REQUESTS = 100;
    private static final String BASE_URL = "http://localhost:8080";
    private static final String JWT_TOKEN ="eyJhbGciOiJIUzM4NCJ9.eyJyb2xlIjoiVVNFUiIsInVzZXJJZCI6MSwic3ViIjoicmFodWwuc2hhcm1hQGV4YW1wbGUuY29tIiwiaWF0IjoxNzg2NDY2ODc1LCJleHAiOjE3ODY1NTMyNzV9.8kOFEdqUckjNGa_2AoiDzpvUX5pZodAXXsnnT3WFxNGE2u48AKWqsnROPe9cNWtm";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldHandleConcurrentBookings() throws Exception {
        //create a pool containing 20 workers threads
        ExecutorService executor = Executors.newFixedThreadPool(20);
        //it enables asynchronous , non blocking programming by representing a future result of a background computation
        //future ,completion interface --> allows us to chain tasks , combine multiple asynchronous operations and handle the errors gracefully
        //without freezing the main application
        List<CompletableFuture<BookingTestResult>> futures = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for(int i =1 ; i <= TOTAL_REQUESTS; i++){
            String idempotencyKey = String.format("DAY22-TEST-%03d", i);

            CompletableFuture<BookingTestResult> future = CompletableFuture
                    .supplyAsync(() -> sendBookingRequest(idempotencyKey), executor);
            //so after this line
            //Future1 -> executor -> thread1  -> send booking request
            //Future2 -. executor -> thread2 -> send booking request
            futures.add(future);
        }
        //.allOf --> wait for all futures to complete before proceeding
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        //dont accept any more tasks shut down when existing tasks finishes
        executor.shutdown();
        long endTime = System.currentTimeMillis();
        //.join()-> blocks the current thread  and waits for the async background task to finish so it can return the final result
        List<BookingTestResult> results = futures.stream().map(CompletableFuture::join).toList();
        printResults(results, endTime - startTime);
        verifyResults(results);


    }
    private BookingTestResult sendBookingRequest(String idempotencyKey) {
        // Implement the logic to send a booking request to the API
        // Use the httpClient to send a POST request to the booking endpoint
        // Include the JWT token in the Authorization header
        // Return a BookingTestResult object containing the requestId and response status
        try{
            String requestBody = """
                    {
                        "trainId": 2,
                        "journeyDate": "2026-08-15",
                        "quotaCode": "GN",
                        "coachType": "SLEEPER",
                        "passengers": [
                             {
                               "name": "Day22 Passenger",
                               "age": 25,
                               "gender": "MALE",
                               "berthPreference": "LOWER"
                             }
                           ]
                    }
                    """;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/v1/bookings"))
                    .header("Content-Type" , "application/json")
                    .header("Authorization", "Bearer " + JWT_TOKEN)
                    .header("Idempotency-Key", idempotencyKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new BookingTestResult(
                    idempotencyKey,
                    response.statusCode(),
                    response.body(),
                    null
            );
        }catch(Exception ex){
            return new BookingTestResult(
                    idempotencyKey,
                    0 ,
                    null,
                    ex.getMessage()
            );

        }

    }
    private record BookingTestResult(
            String idempotencyKey,
            int statusCode,
            String responseBody,
            String exception
    ) {
    }
    private void printResults(
            List<BookingTestResult> results,
            long durationMs
    ) {
        long successful = results.stream()
                        .filter(r -> r.statusCode() == 201)
                        .count();

        long unauthorized = results.stream()
                        .filter(r -> r.statusCode() == 401)
                        .count();

        long badRequest = results.stream()
                        .filter(r -> r.statusCode() == 400)
                        .count();

        long serverErrors = results.stream()
                        .filter(r -> r.statusCode() >= 500)
                        .count();

        long exceptions = results.stream()
                        .filter(r -> r.exception() != null)
                        .count();

        System.out.println();
        System.out.println("==========================================");
        System.out.println("       DAY 22 CONCURRENCY TEST");
        System.out.println("==========================================");
        System.out.println("Total Requests      : " + TOTAL_REQUESTS);
        System.out.println("Duration (ms)       : " + durationMs);
        System.out.println("HTTP 201            : " + successful);
        System.out.println("HTTP 400            : " + badRequest);
        System.out.println("HTTP 401            : " + unauthorized);
        System.out.println("HTTP 5xx            : " + serverErrors);
        System.out.println("Exceptions          : " + exceptions);
        System.out.println("==========================================");

        System.out.println();
        System.out.println("INDIVIDUAL RESULTS");
        System.out.println("------------------------------------------");

        results.forEach(result ->
                System.out.println(
                        result.idempotencyKey()
                                + " | HTTP "
                                + result.statusCode()
                                + (
                                result.exception() != null
                                        ? " | ERROR: "
                                        + result.exception()
                                        : ""
                        )
                )
        );

        System.out.println("------------------------------------------");
    }

    private void verifyResults(List<BookingTestResult> results) {

        long failedRequests = results.stream()
                        .filter(r -> r.exception() != null || r.statusCode() == 0)
                        .count();

        assertEquals(
                0,
                failedRequests,
                "Some concurrent requests were lost"
        );
    }


}
