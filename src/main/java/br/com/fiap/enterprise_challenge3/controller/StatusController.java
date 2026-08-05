package br.com.fiap.enterprise_challenge3.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    @GetMapping
    public ResponseEntity<Map<String, String>> verificarStatus() {
        return ResponseEntity.ok(
                Map.of(
                        "sistema", "GovAtende",
                        "projeto", "Enterprise Challenge 3",
                        "status", "online"
                )
        );
    }
}