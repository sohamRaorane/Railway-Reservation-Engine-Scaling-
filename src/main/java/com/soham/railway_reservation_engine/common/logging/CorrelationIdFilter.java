package com.soham.railway_reservation_engine.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.UUID;

/**
 * Assigns a correlation id to every HTTP request and carries it across the request's logs.
 *
 * <p><b>Distributed-tracing concept:</b> in a system with Kafka consumers, webhooks and async
 * flows, a single user action touches many components. Each request gets a unique id
 * ({@code UUID}) unless an upstream caller already supplied one (header {@code correlationId})
 * — in that case it is propagated unchanged so the whole chain shares one id.
 *
 * <p><b>Advanced logging concept — MDC (Mapped Diagnostic Context):</b> values put in the MDC are
 * per-thread (thread-local storage, invisible to other threads) and can be injected into every
 * log line via the log pattern ({@code %X{correlationId}}). The finally block removes the key so
 * pooled servlet threads don't leak the id into the next request — a classic thread-pool pitfall.
 *
 * <p>Because it extends {@code OncePerRequestFilter}, it also guarantees execution exactly once
 * per request even if the filter is mapped multiple times.
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String CORRELATION_ID = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain

    ){
        //Upstream
        String correlationId = request.getHeader(CORRELATION_ID);
        if(correlationId == null || correlationId.isBlank()){
            correlationId = UUID.randomUUID().toString();
        }
        try{
            //Mdc -> mapped diagnostic context --> it uses the local storage  and maintains the secrey ie the one thread info cannot be accessed by other
            MDC.put(CORRELATION_ID, correlationId);
            response.setHeader(CORRELATION_ID, correlationId);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error processing request", e);
        }
        finally {
            MDC.remove(CORRELATION_ID);
        }

    }

}
