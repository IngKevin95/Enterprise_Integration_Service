package com.empresa.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada del microservicio de integración empresarial. */
@SpringBootApplication
public class EnterpriseIntegrationApplication {

    /**
     * Main method.
     *
     * @param args argumentos de línea de comando
     */
    public static void main(String[] args) {
        SpringApplication.run(EnterpriseIntegrationApplication.class, args);
    }
}
