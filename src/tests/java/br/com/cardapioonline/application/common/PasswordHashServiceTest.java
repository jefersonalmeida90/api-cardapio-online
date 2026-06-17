package br.com.cardapioonline.application.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordHashServiceTest {

    private final PasswordHashService service = new PasswordHashService();

    @Test
    void shouldHashAndVerifyPassword() {
        String hash = service.hash("Senha@123");

        assertThat(hash).contains(".");
        assertThat(service.verify("Senha@123", hash)).isTrue();
        assertThat(service.verify("outra-senha", hash)).isFalse();
    }

    @Test
    void shouldReturnFalseForInvalidStoredHashFormat() {
        assertThat(service.verify("Senha@123", "invalido")).isFalse();
        assertThat(service.verify("Senha@123", "abc.def.ghi")).isFalse();
    }
}
