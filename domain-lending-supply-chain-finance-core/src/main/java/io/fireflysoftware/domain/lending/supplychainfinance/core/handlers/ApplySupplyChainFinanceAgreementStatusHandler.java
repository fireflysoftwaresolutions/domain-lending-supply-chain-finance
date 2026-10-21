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

package io.fireflysoftware.domain.lending.supplychainfinance.core.handlers;

import io.fireflysoftware.core.lending.supplychainfinance.sdk.api.SupplyChainFinanceAgreementApi;
import io.fireflysoftware.core.lending.supplychainfinance.sdk.model.SupplyChainFinanceAgreementDTO;
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.ApplySupplyChainFinanceAgreementStatusCommand;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.annotations.CommandHandlerComponent;
import org.fireflyframework.cqrs.command.CommandHandler;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Updates a supply chain finance agreement, including its status, through the core.
 */
@Slf4j
@CommandHandlerComponent
public class ApplySupplyChainFinanceAgreementStatusHandler extends CommandHandler<ApplySupplyChainFinanceAgreementStatusCommand, SupplyChainFinanceAgreementDTO> {

    private final SupplyChainFinanceAgreementApi api;

    public ApplySupplyChainFinanceAgreementStatusHandler(SupplyChainFinanceAgreementApi api) {
        this.api = api;
    }

    @Override
    protected Mono<SupplyChainFinanceAgreementDTO> doHandle(ApplySupplyChainFinanceAgreementStatusCommand command) {
        log.debug("Updating supply chain finance agreement status: agreementId={}", command.agreementId());
        return api.update4(command.agreementId(), command.record(), UUID.randomUUID().toString());
    }
}
