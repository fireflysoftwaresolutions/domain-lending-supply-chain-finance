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

package io.fireflysoftware.domain.lending.supplychainfinance.core.services;

import io.fireflysoftware.core.lending.supplychainfinance.sdk.model.SupplyChainFinanceAgreementDTO;
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.CreateSupplyChainFinanceAgreementCommand;
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.UpdateSupplyChainFinanceAgreementCommand;
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.UpdateSupplyChainFinanceAgreementStatusCommand;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Application service for supply chain finance agreement orchestration.
 */
public interface SupplyChainFinanceAgreementsService {

    /**
     * Creates a supply chain finance agreement through the register saga.
     *
     * @param command the create command
     * @return the record returned by the core
     */
    Mono<SupplyChainFinanceAgreementDTO> create(CreateSupplyChainFinanceAgreementCommand command);

    /**
     * Moves a supply chain finance agreement to a new status through the core.
     *
     * @param command the status change
     * @return the updated record
     */
    Mono<SupplyChainFinanceAgreementDTO> updateStatus(UpdateSupplyChainFinanceAgreementStatusCommand command);

    /**
     * Reads one supply chain finance agreement.
     *
     * @param agreementId the core identifier
     * @return the record, or empty when the core has none
     */
    Mono<SupplyChainFinanceAgreementDTO> get(UUID agreementId);

    /**
     * Updates fields on a supply chain finance agreement and leaves its status unchanged.
     *
     * @param command the update
     * @return the record returned by the core
     */
    Mono<SupplyChainFinanceAgreementDTO> update(UpdateSupplyChainFinanceAgreementCommand command);

    /**
     * Close supply chain finance agreement when the core allows CLOSED.
     *
     * @param agreementId the core identifier
     * @param statusReason optional reason stored by the status saga when the core keeps one
     * @return the updated record
     */
    Mono<SupplyChainFinanceAgreementDTO> close(UUID agreementId, String statusReason);

    /**
     * Cancel supply chain finance agreement when the core allows TERMINATED.
     *
     * @param agreementId the core identifier
     * @param statusReason optional reason stored by the status saga when the core keeps one
     * @return the updated record
     */
    Mono<SupplyChainFinanceAgreementDTO> cancel(UUID agreementId, String statusReason);

    /**
     * Deletes a supply chain finance agreement when the core lifecycle allows removal.
     *
     * @param agreementId the core identifier
     * @return completion when the core has deleted the record
     */
    Mono<Void> remove(UUID agreementId);
}
