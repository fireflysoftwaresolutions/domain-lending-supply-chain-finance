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

package io.fireflysoftware.domain.lending.supplychainfinance.core.workflows;

import io.fireflysoftware.core.lending.supplychainfinance.sdk.model.SupplyChainFinanceAgreementDTO;
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.ApplySupplyChainFinanceAgreementStatusCommand;
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.GetSupplyChainFinanceAgreementQuery;
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.UpdateSupplyChainFinanceAgreementStatusCommand;

import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.cqrs.query.QueryBus;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.fireflyframework.orchestration.saga.annotation.Saga;
import org.fireflyframework.orchestration.saga.annotation.SagaStep;
import org.fireflyframework.orchestration.saga.annotation.StepEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.COMPENSATE_RESTORE;
import static io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.CTX_PREVIOUS_STATUS;
import static io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.CTX_RECORD;
import static io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.EVENT_RETRIEVED;
import static io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.EVENT_STATUS_UPDATED;
import static io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.SAGA_UPDATE_STATUS;
import static io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.STEP_RETRIEVE;
import static io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.STEP_UPDATE_STATUS;




/**
 * Changes a supply chain finance agreement status by reading it and writing the new status through the core.
 */
@Saga(name = SAGA_UPDATE_STATUS)
@Service
public class UpdateSupplyChainFinanceAgreementStatusSaga {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    @Autowired
    public UpdateSupplyChainFinanceAgreementStatusSaga(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @SagaStep(id = STEP_RETRIEVE)
    @StepEvent(type = EVENT_RETRIEVED)
    public Mono<SupplyChainFinanceAgreementDTO> retrieveSupplyChainFinanceAgreement(GetSupplyChainFinanceAgreementQuery query, ExecutionContext ctx) {
        return queryBus.query(query)
                .doOnNext(record -> ctx.putVariable(CTX_RECORD, record));
    }

    @SagaStep(id = STEP_UPDATE_STATUS, dependsOn = STEP_RETRIEVE, compensate = COMPENSATE_RESTORE)
    @StepEvent(type = EVENT_STATUS_UPDATED)
    public Mono<SupplyChainFinanceAgreementDTO> updateSupplyChainFinanceAgreementStatus(UpdateSupplyChainFinanceAgreementStatusCommand command, ExecutionContext ctx) {
        SupplyChainFinanceAgreementDTO current = ctx.getVariableAs(CTX_RECORD, SupplyChainFinanceAgreementDTO.class);
        if (current == null || current.getId() == null) {
            return Mono.error(new IllegalStateException("Supply chain finance agreement was not found"));
        }
        if (current.getStatus() == null) {
            return Mono.error(new IllegalStateException("Supply chain finance agreement has no current status"));
        }
        if (command.status() == null || command.status().isBlank()) {
            return Mono.error(new IllegalArgumentException("Status is required"));
        }
        ctx.putVariable(CTX_PREVIOUS_STATUS, current.getStatus().getValue());
        current.setStatus(SupplyChainFinanceAgreementDTO.StatusEnum.fromValue(command.status()));
        return commandBus.send(new ApplySupplyChainFinanceAgreementStatusCommand(current.getId(), current))
                .doOnNext(updated -> ctx.putVariable(CTX_RECORD, updated));
    }

    public Mono<Void> restoreSupplyChainFinanceAgreementStatus(SupplyChainFinanceAgreementDTO updated, ExecutionContext ctx) {
        if (updated == null || updated.getId() == null) {
            return Mono.empty();
        }
        String previous = ctx.getVariableAs(CTX_PREVIOUS_STATUS, String.class);
        if (previous == null || previous.isBlank()) {
            return Mono.empty();
        }
        updated.setStatus(SupplyChainFinanceAgreementDTO.StatusEnum.fromValue(previous));
        return commandBus.send(new ApplySupplyChainFinanceAgreementStatusCommand(updated.getId(), updated)).then();
    }

}
