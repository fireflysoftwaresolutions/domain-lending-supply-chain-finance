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
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.GetSupplyChainFinanceAgreementQuery;
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.RemoveSupplyChainFinanceAgreementCommand;
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.UpdateSupplyChainFinanceAgreementCommand;
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.UpdateSupplyChainFinanceAgreementStatusCommand;
import io.fireflysoftware.domain.lending.supplychainfinance.core.lifecycle.SupplyChainFinanceAgreementLifecycle;
import io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants;
import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.cqrs.query.QueryBus;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Runs the supply chain finance agreement sagas and reads through the query bus.
 */
@Service
public class SupplyChainFinanceAgreementsServiceImpl implements SupplyChainFinanceAgreementsService {

    private final SagaEngine sagaEngine;
    private final QueryBus queryBus;
    private final CommandBus commandBus;

    public SupplyChainFinanceAgreementsServiceImpl(SagaEngine sagaEngine, QueryBus queryBus, CommandBus commandBus) {
        this.sagaEngine = sagaEngine;
        this.queryBus = queryBus;
        this.commandBus = commandBus;
    }

    @Override
    public Mono<SupplyChainFinanceAgreementDTO> create(CreateSupplyChainFinanceAgreementCommand command) {
        StepInputs inputs = StepInputs.builder()
                .forStepId(io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.STEP_REGISTER, command)
                .build();
        return sagaEngine.execute(io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.SAGA_REGISTER, inputs)
                .flatMap(result -> outcome(result, io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.STEP_REGISTER, SupplyChainFinanceAgreementDTO.class));
    }

    @Override
    public Mono<SupplyChainFinanceAgreementDTO> updateStatus(UpdateSupplyChainFinanceAgreementStatusCommand command) {
        StepInputs inputs = StepInputs.builder()
                .forStepId(io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.STEP_RETRIEVE, new GetSupplyChainFinanceAgreementQuery(command.agreementId()))
                .forStepId(io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.STEP_UPDATE_STATUS, command)
                .build();
        return sagaEngine.execute(io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.SAGA_UPDATE_STATUS, inputs)
                .flatMap(result -> outcome(result, io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.STEP_UPDATE_STATUS, SupplyChainFinanceAgreementDTO.class));
    }

    @Override
    public Mono<SupplyChainFinanceAgreementDTO> get(UUID agreementId) {
        return queryBus.query(new GetSupplyChainFinanceAgreementQuery(agreementId));
    }

    @Override
    public Mono<SupplyChainFinanceAgreementDTO> update(UpdateSupplyChainFinanceAgreementCommand command) {
        StepInputs inputs = StepInputs.builder()
                .forStepId(io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.STEP_RETRIEVE_FOR_UPDATE, new GetSupplyChainFinanceAgreementQuery(command.agreementId()))
                .forStepId(io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.STEP_UPDATE_RECORD, command)
                .build();
        return sagaEngine.execute(io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.SAGA_UPDATE_RECORD, inputs)
                .flatMap(result -> outcome(result, io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.STEP_UPDATE_RECORD, SupplyChainFinanceAgreementDTO.class));
    }

    @Override
    public Mono<SupplyChainFinanceAgreementDTO> close(UUID agreementId, String statusReason) {
        return transition(agreementId, SupplyChainFinanceAgreementDTO.StatusEnum.CLOSED, statusReason);
    }

    @Override
    public Mono<SupplyChainFinanceAgreementDTO> cancel(UUID agreementId, String statusReason) {
        return transition(agreementId, SupplyChainFinanceAgreementDTO.StatusEnum.TERMINATED, statusReason);
    }

    private Mono<SupplyChainFinanceAgreementDTO> transition(UUID agreementId, SupplyChainFinanceAgreementDTO.StatusEnum target, String statusReason) {
        return get(agreementId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Supply chain finance agreement was not found")))
                .flatMap(current -> {
                    if (!SupplyChainFinanceAgreementLifecycle.canTransition(current.getStatus(), target)) {
                        String from = current.getStatus() == null
                                ? "unset"
                                : current.getStatus().getValue();
                        return Mono.<SupplyChainFinanceAgreementDTO>error(new IllegalStateException(
                                "Supply chain finance agreement " + agreementId + " cannot move from " + from + " to " + target.getValue()));
                    }
                    return updateStatus(new UpdateSupplyChainFinanceAgreementStatusCommand(agreementId, target.getValue(), statusReason));
                });
    }

    @Override
    public Mono<Void> remove(UUID agreementId) {
        return get(agreementId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Supply chain finance agreement was not found")))
                .flatMap(current -> {
                    if (!SupplyChainFinanceAgreementLifecycle.canRemove(current.getStatus())) {
                        String statusValue = current.getStatus() == null
                                ? "unset"
                                : current.getStatus().getValue();
                        return Mono.error(new IllegalStateException(
                                "Supply chain finance agreement " + agreementId + " is " + statusValue
                                        + " and cannot be deleted; terminate it so the agreement is kept"));
                    }
                    return commandBus.send(new RemoveSupplyChainFinanceAgreementCommand(agreementId));
                });
    }

    private static <T> Mono<T> outcome(SagaResult result, String stepId, Class<T> type) {
        if (result.isFailed()) {
            Throwable error = result.error().orElseGet(() ->
                    new IllegalStateException("Saga " + result.sagaName() + " failed"));
            return Mono.error(error);
        }
        return Mono.justOrEmpty(result.resultOf(stepId, type))
                .switchIfEmpty(Mono.error(new IllegalStateException("Saga step " + stepId + " produced no result")));
    }
}
