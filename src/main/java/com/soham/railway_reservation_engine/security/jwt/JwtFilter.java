package com.soham.railway_reservation_engine.security.jwt;

import com.soham.railway_reservation_engine.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that authenticates every request from its {@code Authorization: Bearer <jwt>}
 * header, BEFORE the request reaches any controller.
 *
 * <p><b>Filter flow:</b>
 * <ol>
 *   <li>No/invalid header → pass through unauthenticated (the security config decides whether
 *       that endpoint needs auth anyway).</li>
 *   <li>Extract the token (after the "Bearer " prefix) and its username (subject).</li>
 *   <li>If no authentication exists yet in the {@code SecurityContextHolder} (a thread-local
 *       store of "who is the current user"), load the user and validate the token against them.</li>
 *   <li>On success, build a {@code UsernamePasswordAuthenticationToken} and store it in the
 *       security context — the controller then reads {@code @AuthenticationPrincipal} from it.</li>
 * </ol>
 *
 * <p>Extends {@code OncePerRequestFilter}, guaranteeing single execution per request. Extending
 * the stateless JWT pattern means no server-side sessions — any instance can validate any request.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter  extends OncePerRequestFilter  {
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;


    //automatically called for every incoming HTTP request
    @Override
    protected  void doFilterInternal(
            @NonNull HttpServletRequest request, //browser req
            @NonNull HttpServletResponse response, //which is going back to the client
            @NonNull FilterChain filterChain //the filters which are remaining
            ) throws ServletException, IOException {
        // Implement the filter logic here
        // This method will be called for each incoming request
        // You can extract the JWT token from the request header, validate it, and set the authentication in the security context
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            //if the check fails --> i am not authenticating this request  pass it to the next filter
            filterChain.doFilter(request,response);
            return; // goback
        }
        jwt = authHeader.substring(7); //7th index is my first jwt char that  i want to extract
        username = jwtService.extractUsername(jwt);



        //SecurityContextHolder --> current logged in  user hai uska storage
        //it means that the security context  do not have any kind of user authentication information and if it is not null
        //then someone has already authenticated the request so do not authenticate it again
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            if(jwtService.isTokenValid(jwt, userDetails)) {

                //this auth token represent that the user has been authenticated
                //So it means that -->

                /*
                Principal is set to userdetails
                credentials are set to null --> because after auth we do not need to store the credentials anymore
                and the auth
                 */
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    //this stores the request specific details like the IP adddress and the session id
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    //it means that the current request is authenticated and the user is also a authenticated user
                    SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        //Jwt filter has finished now move towards the next filter if it exsits
        //and if this line is not returned then the request stops and do not reaches to the controller and the controller will never execute
        filterChain.doFilter(request,response);
    }
}

/*
Complete flow of the JWT filter:-

                    Client
                    ↓
                    Authorization Header
                    ↓
                    JwtFilter
                    ↓
                    Read Header
                    \
                    Bearer Present?
                    ↓
                    Extract JWT
                    ↓
                    Extract Username
                    ↓
                    Load User
                    ↓
                    Validate Token
                    ↓
                    Create Authentication Token
                    ↓
                    Store in SecurityContext
                    ↓
                    Continue Filter Chain
                    ↓
                    Controller
                    ↓
                    Response
 */
