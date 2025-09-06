/*
 * Copyright (c) 2025 Escuela Colombiana de Ingenieria Julio Garavito.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 *
 *     https://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 */

package co.eci.blacklist.api;

import java.net.UnknownHostException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.eci.blacklist.api.dto.CheckResponseDTO;
import co.eci.blacklist.application.BlacklistService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * REST controller for blacklist verification operations.
 *
 * @author ARSW-PANDILLA-2025
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/blacklist")
public class BlacklistController {

    /** The blacklist service for business logic operations */
    private final BlacklistService service;

    /**
     * Constructs a new BlacklistController with the specified service.
     *
     * @param service the blacklist service for handling business logic
     * @throws IllegalArgumentException if service is null
     */
    public BlacklistController(BlacklistService service) {
        this.service = service;
    }

    /**
     * Verifies an IP address against blacklist servers using parallel processing.
     *
     * @param ip      The IPv4 address to verify.
     * @param threads The number of threads to use (0 for automatic detection).
     * @return Verification result wrapped in CheckResponseDTO or 400 if IP invalid.
     */
    @GetMapping("/check")
    public ResponseEntity<?> check(
            @RequestParam String ip,
            @RequestParam(defaultValue = "0") @Min(0) @Max(10_000) int threads) {

        // Validates IP first
        if (!isValidIp(ip)) {
            return ResponseEntity.badRequest().body("Invalid IP address: " + ip);
        }

        int effectiveThreads = threads > 0 ? threads : Math.max(1, Runtime.getRuntime().availableProcessors());
        var res = service.check(ip, effectiveThreads);
        return ResponseEntity.ok(CheckResponseDTO.from(res));
    }

    /**
     * Validates whether a string is a valid IPv4 or IPv6 address.
     *
     * @param ip the string to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidIp(String ip) {
        try {
            java.net.InetAddress inet = java.net.InetAddress.getByName(ip);
            // Ensure that the parsed IP matches the input (avoid accepting things like hostnames)
            return inet.getHostAddress().equals(ip);
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
