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

/*
so every http request gets oyu a correlation id
if the correlation id is not present then set it using the random uuid
otherwise use the header which is given by the application
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
