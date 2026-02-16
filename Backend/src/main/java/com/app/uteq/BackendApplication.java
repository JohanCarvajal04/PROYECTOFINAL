package com.app.uteq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.app.uteq.Config.RsaKeyConfig;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyConfig.class)
public class BackendApplication {


    
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * Bean para codificar contraseñas con BCrypt.
     * BCrypt genera un salt aleatorio en cada hash.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
