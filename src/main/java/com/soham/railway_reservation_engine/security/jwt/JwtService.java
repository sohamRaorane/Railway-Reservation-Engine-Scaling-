package com.soham.railway_reservation_engine.security.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * Generates, reads and validates JWT access tokens.
 *
 * <p><b>Token anatomy:</b> a JWT is a base64url triple — header, claims, signature. The claims of
 * the tokens issued here are:
 * <pre>
 * { "sub": "sohamraorane08@gmail.com", "iat": ..., "exp": ... }
 * </pre>
 * {@code sub} is the username (email), {@code iat}/{@code exp} bound the validity window. The
 * signature is HMAC-SHA256 over header+claims with a secret key known only to the server, so a
 * client cannot forge or tamper with claims (verification would fail).
 *
 * <p><b>Advanced Java:</b> {@code extractClaim} is generic ({@code <T>}) and takes a
 * {@code Function<Claims, T>} — a <i>functional interface</i> — so callers pass small lambdas
 * like {@code claims -> claims.getSubject()} to pull out any single claim without duplicating the
 * parsing logic. {@code generateToken} is overloaded: the two-arg variant adds extra claims, the
 * one-arg variant delegates to it with an empty map.
 *
 * <p>Signing keys are derived from a base64 secret via {@code Keys.hmacShaKeyFor} — crypto
 * algorithms operate on raw bytes, not strings.
 */
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMillis;


    //Method overloading
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraclaims, UserDetails userDetails) {
        // Implement token generation logic here
        return Jwts.builder()
                .claims(extraclaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationInMillis))
                .signWith(getSigningKey())
                .compact();
    }
    public String extractUsername(String token) {
        //read all the claims from the jwt and then return the subject
        return extractClaim(token, claims -> claims.getSubject());

    }

    //Functional Interface --> give me a function that accpets the claim and returns some type t
    //here type T can be anything
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); //this helps to extract all the claims from the token
        return claimsResolver.apply(claims); // now this line executes the claims function that was passed
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return  (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    //token expiration time is it before the curru time if it return false then the token is valid
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    private Date extractExpiration(String token) {
        return extractClaim(token, claims -> claims.getExpiration());
    }

    //used to read the jwt
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    //crypto graphic algos they worl with the byte not string hence we need to decode the string to byte and then use it to sign the token
    private SecretKey getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        //now a proper cryptographic secret key is generated
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
