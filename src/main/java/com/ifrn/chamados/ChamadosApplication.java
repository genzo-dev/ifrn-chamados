package com.ifrn.chamados;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController 
public class ChamadosApplication {

    private final String envActive;

    public ChamadosApplication(
            @Value("${app.environment}") String envActive) {
        this.envActive = envActive;
    }

    @GetMapping("/")
    public String home() {
        return "API de Chamados está funcionando! Ambiente: " + envActive;
    }

	public static void main(String[] args) {
		SpringApplication.run(ChamadosApplication.class, args);
	}

}
