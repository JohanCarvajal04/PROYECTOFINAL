package com.app.uteq.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Record que vincula las propiedades rsa.public-key y rsa.private-key
 * del application.properties a objetos RSA tipados.
 *
 * Spring Boot automáticamente:
 * 1. Lee los archivos PEM desde la ruta indicada
 * 2. Parsea el contenido PEM a objetos RSA
 * 3. Inyecta los objetos en este record
 */
@ConfigurationProperties(prefix = "rsa")
public record RsaKeyConfig(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
}
