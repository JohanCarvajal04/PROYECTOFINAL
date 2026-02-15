package com.app.uteq;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad para generar par de claves RSA (2048 bits) en formato PEM.
 * Ejecutar una sola vez para crear los archivos public.pem y private.pem.
 */
public class GenerateKeyPair {

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        var keyPair = keyPairGenerator.generateKeyPair();

        byte[] pub = keyPair.getPublic().getEncoded();
        byte[] pri = keyPair.getPrivate().getEncoded();

        // Crear directorio si no existe
        new File("src/main/resources/certs").mkdirs();

        // Escribir clave pública
        try (PemWriter pemWriter = new PemWriter(
                new OutputStreamWriter(new FileOutputStream("src/main/resources/certs/public.pem")))) {
            pemWriter.writeObject(new PemObject("PUBLIC KEY", pub));
        }

        // Escribir clave privada
        try (PemWriter pemWriter = new PemWriter(
                new OutputStreamWriter(new FileOutputStream("src/main/resources/certs/private.pem")))) {
            pemWriter.writeObject(new PemObject("PRIVATE KEY", pri));
        }

        System.out.println("Claves RSA generadas en src/main/resources/certs/");
    }
}
