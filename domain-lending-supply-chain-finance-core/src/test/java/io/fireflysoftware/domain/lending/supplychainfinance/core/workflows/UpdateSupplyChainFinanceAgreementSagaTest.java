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
import io.fireflysoftware.domain.lending.supplychainfinance.core.commands.UpdateSupplyChainFinanceAgreementCommand;
import io.fireflysoftware.domain.lending.supplychainfinance.core.constants.SupplyChainFinanceAgreementSagaConstants;
import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.cqrs.query.QueryBus;
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
 * Verifies a field update keeps the current status and can be restored.
 */
@ExtendWith(MockitoExtension.class)
class UpdateSupplyChainFinanceAgreementSagaTest {

    @Mock
    private CommandBus commandBus;

    @Mock
    private QueryBus queryBus;

    private UpdateSupplyChainFinanceAgreementSaga saga;

    @BeforeEach
    void setUp() {
        saga = new UpdateSupplyChainFinanceAgreementSaga(commandBus, queryBus);
    }

    @Test
    void update_keepsTheCurrentStatus() {
        UUID id = UUID.randomUUID();
        SupplyChainFinanceAgreementDTO current = new SupplyChainFinanceAgreementDTO(id);
        current.setStatus(SupplyChainFinanceAgreementDTO.StatusEnum.DRAFT);
        SupplyChainFinanceAgreementDTO incoming = new SupplyChainFinanceAgreementDTO();
        incoming.setStatus(SupplyChainFinanceAgreementDTO.StatusEnum.ACTIVE);
        when(commandBus.send(any(ApplySupplyChainFinanceAgreementStatusCommand.class))).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0, ApplySupplyChainFinanceAgreementStatusCommand.class).record()));

        ExecutionContext ctx = ExecutionContext.forSaga(SupplyChainFinanceAgreementSagaConstants.SAGA_UPDATE_RECORD, "test");
        ctx.putVariable(SupplyChainFinanceAgreementSagaConstants.CTX_RECORD, current);

        StepVerifier.create(saga.updateSupplyChainFinanceAgreement(new UpdateSupplyChainFinanceAgreementCommand(id, incoming), ctx))
                .assertNext(saved -> assertThat(saved.getStatus()).isEqualTo(SupplyChainFinanceAgreementDTO.StatusEnum.DRAFT))
                .verifyComplete();

        ArgumentCaptor<ApplySupplyChainFinanceAgreementStatusCommand> captor = ArgumentCaptor.forClass(ApplySupplyChainFinanceAgreementStatusCommand.class);
        verify(commandBus).send(captor.capture());
        assertThat(captor.getValue().agreementId()).isEqualTo(id);
        assertThat(captor.getValue().record().getStatus()).isEqualTo(SupplyChainFinanceAgreementDTO.StatusEnum.DRAFT);
        assertThat(ctx.getVariableAs(SupplyChainFinanceAgreementSagaConstants.CTX_PREVIOUS_RECORD, SupplyChainFinanceAgreementDTO.class)).isSameAs(current);
    }

    @Test
    void update_passesReverseFactoringThrough() {
        UUID id = UUID.randomUUID();
        SupplyChainFinanceAgreementDTO current = new SupplyChainFinanceAgreementDTO(id);
        current.setStatus(SupplyChainFinanceAgreementDTO.StatusEnum.DRAFT);
        current.setFinanceType(SupplyChainFinanceAgreementDTO.FinanceTypeEnum.FACTORING);
        SupplyChainFinanceAgreementDTO incoming = new SupplyChainFinanceAgreementDTO();
        incoming.setFinanceType(SupplyChainFinanceAgreementDTO.FinanceTypeEnum.REVERSE_FACTORING);
        when(commandBus.send(any(ApplySupplyChainFinanceAgreementStatusCommand.class))).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0, ApplySupplyChainFinanceAgreementStatusCommand.class).record()));

        ExecutionContext ctx = ExecutionContext.forSaga(SupplyChainFinanceAgreementSagaConstants.SAGA_UPDATE_RECORD, "test");
        ctx.putVariable(SupplyChainFinanceAgreementSagaConstants.CTX_RECORD, current);

        StepVerifier.create(saga.updateSupplyChainFinanceAgreement(new UpdateSupplyChainFinanceAgreementCommand(id, incoming), ctx))
                .assertNext(saved -> assertThat(saved.getFinanceType())
                        .isEqualTo(SupplyChainFinanceAgreementDTO.FinanceTypeEnum.REVERSE_FACTORING))
                .verifyComplete();
    }

    @Test
    void restore_writesThePreviousRecordBack() {
        UUID id = UUID.randomUUID();
        SupplyChainFinanceAgreementDTO previous = new SupplyChainFinanceAgreementDTO(id);
        previous.setStatus(SupplyChainFinanceAgreementDTO.StatusEnum.DRAFT);
        SupplyChainFinanceAgreementDTO updated = new SupplyChainFinanceAgreementDTO(id);
        updated.setStatus(SupplyChainFinanceAgreementDTO.StatusEnum.ACTIVE);
        when(commandBus.send(any(ApplySupplyChainFinanceAgreementStatusCommand.class))).thenReturn(Mono.just(previous));

        ExecutionContext ctx = ExecutionContext.forSaga(SupplyChainFinanceAgreementSagaConstants.SAGA_UPDATE_RECORD, "test");
        ctx.putVariable(SupplyChainFinanceAgreementSagaConstants.CTX_PREVIOUS_RECORD, previous);

        StepVerifier.create(saga.restoreSupplyChainFinanceAgreement(updated, ctx))
                .verifyComplete();

        ArgumentCaptor<ApplySupplyChainFinanceAgreementStatusCommand> captor = ArgumentCaptor.forClass(ApplySupplyChainFinanceAgreementStatusCommand.class);
        verify(commandBus).send(captor.capture());
        assertThat(captor.getValue().record()).isSameAs(previous);
    }
}
