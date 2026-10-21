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
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.CreateSupplyChainFinanceAgreementCommand;
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.RemoveSupplyChainFinanceAgreementCommand;

import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.fireflyframework.orchestration.saga.annotation.Saga;
import org.fireflyframework.orchestration.saga.annotation.SagaStep;
import org.fireflyframework.orchestration.saga.annotation.StepEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.COMPENSATE_REMOVE;
import static io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.CTX_RECORD;
import static io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.EVENT_REGISTERED;
import static io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.SAGA_REGISTER;
import static io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants.STEP_REGISTER;




/**
 * Creates a supply chain finance agreement on {@code core-lending-supply-chain-finance} and compensates by deleting it.
 */
@Saga(name = SAGA_REGISTER)
@Service
public class RegisterSupplyChainFinanceAgreementSaga {

    private final CommandBus commandBus;

    @Autowired
    public RegisterSupplyChainFinanceAgreementSaga(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @SagaStep(id = STEP_REGISTER, compensate = COMPENSATE_REMOVE)
    @StepEvent(type = EVENT_REGISTERED)
    public Mono<SupplyChainFinanceAgreementDTO> registerSupplyChainFinanceAgreement(CreateSupplyChainFinanceAgreementCommand command, ExecutionContext ctx) {
        return commandBus.send(command)
                .doOnNext(created -> ctx.putVariable(CTX_RECORD, created));
    }

    public Mono<Void> removeSupplyChainFinanceAgreement(SupplyChainFinanceAgreementDTO created) {
        if (created == null || created.getId() == null) {
            return Mono.empty();
        }
        return commandBus.send(new RemoveSupplyChainFinanceAgreementCommand(created.getId()));
    }

}
