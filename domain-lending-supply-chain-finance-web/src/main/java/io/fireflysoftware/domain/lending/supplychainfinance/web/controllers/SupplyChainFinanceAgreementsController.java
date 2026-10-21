/*
 * Copyright 2026 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fireflysoftware.domain.lending.supplychainfinance.web.controllers;

import io.fireflysoftware.core.lending.supplychainfinance.sdk.model.SupplyChainFinanceAgreementDTO;
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.CreateSupplyChainFinanceAgreementCommand;
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.UpdateSupplyChainFinanceAgreementCommand;
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.UpdateSupplyChainFinanceAgreementStatusCommand;
import io.fireflysoftware.domain.lending.supplychainfinance.core.services.SupplyChainFinanceAgreementsService;
import io.fireflysoftware.domain.lending.supplychainfinance.interfaces.dtos.ReasonRequest;
import io.fireflysoftware.domain.lending.supplychainfinance.interfaces.dtos.StatusChangeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Domain API for supply chain finance agreements.
 */
@RestController
@RequestMapping("/api/v1/supply-chain-finance-agreements")
@Tag(name = "SupplyChainFinanceAgreements", description = "Domain orchestration for supply chain finance agreements")
public class SupplyChainFinanceAgreementsController {

    private final SupplyChainFinanceAgreementsService service;

    public SupplyChainFinanceAgreementsController(SupplyChainFinanceAgreementsService service) {
        this.service = service;
    }

    @Operation(summary = "Create supply chain finance agreement", description = "Runs the register saga against core-lending-supply-chain-finance.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplyChainFinanceAgreementDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SupplyChainFinanceAgreementDTO>> create(@RequestBody SupplyChainFinanceAgreementDTO body) {
        return service.create(new CreateSupplyChainFinanceAgreementCommand(body))
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created));
    }

    @Operation(summary = "Get supply chain finance agreement", description = "Reads one record from core-lending-supply-chain-finance.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplyChainFinanceAgreementDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping(value = "/{agreementId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SupplyChainFinanceAgreementDTO>> get(
            @Parameter(description = "Core identifier", required = true) @PathVariable UUID agreementId) {
        return service.get(agreementId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Update status", description = "Runs the status saga against core-lending-supply-chain-finance.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplyChainFinanceAgreementDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PutMapping(value = "/{agreementId}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SupplyChainFinanceAgreementDTO>> updateStatus(
            @Parameter(description = "Core identifier", required = true) @PathVariable UUID agreementId,
            @RequestBody StatusChangeRequest request) {
        return service.updateStatus(new UpdateSupplyChainFinanceAgreementStatusCommand(agreementId, request.getStatus(), request.getStatusReason()))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Update supply chain finance agreement", description = "Runs the update saga against core-lending-supply-chain-finance. Status is left unchanged.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplyChainFinanceAgreementDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PutMapping(value = "/{agreementId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SupplyChainFinanceAgreementDTO>> update(
            @Parameter(description = "Core identifier", required = true) @PathVariable UUID agreementId,
            @RequestBody SupplyChainFinanceAgreementDTO body) {
        return service.update(new UpdateSupplyChainFinanceAgreementCommand(agreementId, body))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Close supply chain finance agreement", description = "Closes an agreement when the supply chain finance core allows CLOSED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplyChainFinanceAgreementDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping(value = "/{agreementId}/close", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SupplyChainFinanceAgreementDTO>> close(
            @Parameter(description = "Core identifier", required = true) @PathVariable UUID agreementId,
            @RequestBody ReasonRequest request) {
        return service.close(agreementId, request.getStatusReason())
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Cancel supply chain finance agreement", description = "Cancels an agreement by writing TERMINATED when the supply chain finance core allows that move.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplyChainFinanceAgreementDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping(value = "/{agreementId}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SupplyChainFinanceAgreementDTO>> cancel(
            @Parameter(description = "Core identifier", required = true) @PathVariable UUID agreementId,
            @RequestBody ReasonRequest request) {
        return service.cancel(agreementId, request.getStatusReason())
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Remove supply chain finance agreement", description = "Deletes the record through core-lending-supply-chain-finance when that core allows removal.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removed"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @DeleteMapping("/{agreementId}")
    public Mono<ResponseEntity<Void>> remove(
            @Parameter(description = "Core identifier", required = true) @PathVariable UUID agreementId) {
        return service.remove(agreementId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
