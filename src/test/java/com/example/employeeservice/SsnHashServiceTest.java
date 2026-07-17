package com.example.employeeservice.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SsnHashServiceTest {

    private final SsnHashService ssnHashService = new SsnHashService("unit-test-secret-key");

    @Test
    void sameSsnProducesSameHash() {
        String hash1 = ssnHashService.hash("123-45-6789");
        String hash2 = ssnHashService.hash("123-45-6789");

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void differentSsnsProduceDifferentHashes() {
        String hash1 = ssnHashService.hash("123-45-6789");
        String hash2 = ssnHashService.hash("987-65-4321");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void hashDoesNotContainOrEqualRawSsn() {
        String rawSsn = "123-45-6789";
        String hash = ssnHashService.hash(rawSsn);

        assertThat(hash).isNotEqualTo(rawSsn);
        assertThat(hash).doesNotContain("123456789");
    }

    @Test
    void formattingDoesNotAffectHash() {
        String hashWithDashes = ssnHashService.hash("123-45-6789");
        String hashWithoutDashes = ssnHashService.hash("123456789");

        assertThat(hashWithDashes).isEqualTo(hashWithoutDashes);
    }

    @Test
    void hashIsHexEncodedSha256Length() {
        String hash = ssnHashService.hash("123-45-6789");

        // HMAC-SHA256 -> 32 bytes -> 64 hex characters
        assertThat(hash).hasSize(64);
        assertThat(hash).matches("^[0-9a-f]{64}$");
    }

    @Test
    void differentSecretsProduceDifferentHashesForSameSsn() {
        SsnHashService other = new SsnHashService("a-completely-different-secret");

        String hash1 = ssnHashService.hash("123-45-6789");
        String hash2 = other.hash("123-45-6789");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void nullSsnIsRejected() {
        assertThatThrownBy(() -> ssnHashService.hash(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankSecretIsRejectedAtConstruction() {
        assertThatThrownBy(() -> new SsnHashService(" "))
                .isInstanceOf(IllegalStateException.class);
    }
}
