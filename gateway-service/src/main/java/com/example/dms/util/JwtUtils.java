package com.example.dms.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtils {

    // In a real-world scenario, store the secret securely (e.g., in Vault, environment, etc.)
    private final String secretKey = "0v0IamWatchingYou/-v-AreYouLookingForTheKey/-_-|ThereIsNothingForYouHere";
    private final Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

    /**
     * Validate the token and throw exceptions if invalid.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);  // throws if invalid
            return true;
        } catch (JwtException ex) {
            // log error if needed
            return false;
        }
    }

    /**
     * Extract all claims from the token.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract roles claim as a List<String>.
     */
    @SuppressWarnings("unchecked")
    public String extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Extract username (subject) from token.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extract token from "Authorization" header value that starts with "Bearer ".
     * E.g., "Bearer abc.def.ghi" -> "abc.def.ghi"
     */
    public String parseBearerToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
