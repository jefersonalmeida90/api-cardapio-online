package br.com.cardapioonline;

import br.com.cardapioonline.infrastructure.config.AdminAuthProperties;
import br.com.cardapioonline.infrastructure.config.ApiProperties;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableConfigurationProperties({ApiProperties.class, AdminAuthProperties.class})
public class CardapioOnlineJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardapioOnlineJavaApplication.class, args);
    }
}
