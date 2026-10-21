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
import io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants;

import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the register saga stores the core result and can compensate by deleting it.
 */
@ExtendWith(MockitoExtension.class)
class RegisterSupplyChainFinanceAgreementSagaTest {

    @Mock
    private CommandBus commandBus;

    private RegisterSupplyChainFinanceAgreementSaga saga;

    @BeforeEach
    void setUp() {
        saga = new RegisterSupplyChainFinanceAgreementSaga(commandBus);
    }

    @Test
    void register_storesTheRecordReturnedByTheCore() {
        UUID id = UUID.randomUUID();
        SupplyChainFinanceAgreementDTO created = new SupplyChainFinanceAgreementDTO(id);
        created.setStatus(SupplyChainFinanceAgreementDTO.StatusEnum.DRAFT);
        when(commandBus.send(any(CreateSupplyChainFinanceAgreementCommand.class))).thenReturn(Mono.just(created));

        ExecutionContext ctx = ExecutionContext.forSaga(SupplyChainFinanceAgreementSagaConstants.SAGA_REGISTER, "test");
        SupplyChainFinanceAgreementDTO request = new SupplyChainFinanceAgreementDTO();
        request.setStatus(SupplyChainFinanceAgreementDTO.StatusEnum.DRAFT);

        StepVerifier.create(saga.registerSupplyChainFinanceAgreement(new CreateSupplyChainFinanceAgreementCommand(request), ctx))
                .expectNext(created)
                .verifyComplete();

        assertThat(ctx.getVariableAs(SupplyChainFinanceAgreementSagaConstants.CTX_RECORD, SupplyChainFinanceAgreementDTO.class).getId()).isEqualTo(id);
    }

    @Test
    void remove_sendsDeleteForTheCreatedRecord() {
        UUID id = UUID.randomUUID();
        when(commandBus.send(any(RemoveSupplyChainFinanceAgreementCommand.class))).thenReturn(Mono.empty());

        StepVerifier.create(saga.removeSupplyChainFinanceAgreement(new SupplyChainFinanceAgreementDTO(id)))
                .verifyComplete();

        ArgumentCaptor<RemoveSupplyChainFinanceAgreementCommand> captor = ArgumentCaptor.forClass(RemoveSupplyChainFinanceAgreementCommand.class);
        verify(commandBus).send(captor.capture());
        assertThat(captor.getValue().agreementId()).isEqualTo(id);
    }

}
