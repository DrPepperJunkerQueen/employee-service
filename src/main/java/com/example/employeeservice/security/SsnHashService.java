package com.example.employeeservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Turns a raw social security number into a one-way, keyed digest
 * (HMAC-SHA256) that is safe to persist and use for equality/uniqueness
 * checks, but cannot be reversed back into the original SSN.
 *
 * Why hashing instead of (reversible) encryption for this use case:
 *
 * - The service has no requirement anywhere to display, export, or send
 *   the original SSN back out again - every endpoint only ever needs to
 *   know "is this the same SSN as an existing record?", never "what is
 *   the SSN?". That's exactly what a one-way hash is for.
 * - Using a *keyed* hash (HMAC with a server-side secret) instead of a
 *   plain SHA-256 hash matters because SSNs only have ~10^9 possible
 *   values - a plain fast hash would be brute-forceable via a
 *   precomputed lookup table in seconds. Keying it with a secret that
 *   only the server holds defeats that, similar in spirit to a
 *   password "pepper".
 * - The trade-off vs. AES-style encryption: encryption would let us
 *   recover the original SSN later (useful if, say, a downstream
 *   payroll/tax integration legitimately needed the real number), but
 *   that reversibility is itself a liability - anyone who compromises
 *   the encryption key can recover every SSN in the database. Since
 *   this service never needs the SSN back, we don't take on that risk.
 *   If a future requirement needed the real SSN again, the pragmatic
 *   move would be to add an AES-GCM encrypted column *alongside* this
 *   hash (used only for the uniqueness check), with the encryption key
 *   held in a proper secrets manager / KMS rather than app config.
 * - We are not using a slow adaptive hash like BCrypt/Argon2 here
 *   (the usual choice for passwords) because those generate a random
 *   salt per record by design, which makes "does this SSN already
 *   exist?" an O(n) operation (re-derive and compare against every
 *   stored hash). A deterministic keyed HMAC lets the database enforce
 *   uniqueness directly via a unique index, which is what we want for
 *   an identifier like an SSN.
 */
@Service
public class SsnHashService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final Mac macTemplate;

    public SsnHashService(@Value("${app.ssn.hmac-secret}") String hmacSecret) {
        if (hmacSecret == null || hmacSecret.isBlank()) {
            throw new IllegalStateException("app.ssn.hmac-secret must be configured");
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(keySpec);
            this.macTemplate = mac;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Unable to initialize SSN hashing", e);
        }
    }

    /**
     * Hashes a raw SSN (any common formatting like "123-45-6789" is
     * normalized to digits-only first, so "123-45-6789" and "123456789"
     * hash identically).
     */
    public String hash(String rawSsn) {
        if (rawSsn == null) {
            throw new IllegalArgumentException("SSN must not be null");
        }
        String normalized = rawSsn.replaceAll("[^0-9]", "");
        try {
            // Mac is not guaranteed thread-safe across concurrent calls,
            // so each call works on its own clone of the initialized template.
            Mac mac = (Mac) macTemplate.clone();
            byte[] digest = mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Mac implementation does not support cloning", e);
        }
    }
}
