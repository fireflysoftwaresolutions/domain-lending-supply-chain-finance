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

package io.fireflysoftware.domain.lending.supplychainfinance.core.lifecycle;

import io.fireflysoftware.core.lending.supplychainfinance.sdk.model.SupplyChainFinanceAgreementDTO;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Moves copied from the agreement lifecycle on {@code core-lending-supply-chain-finance}.
 * A missing status is treated as DRAFT, which is the only status the core will delete.
 */
public final class SupplyChainFinanceAgreementLifecycle {

    private static final Map<SupplyChainFinanceAgreementDTO.StatusEnum, Set<SupplyChainFinanceAgreementDTO.StatusEnum>> ALLOWED = Map.of(
            SupplyChainFinanceAgreementDTO.StatusEnum.DRAFT, EnumSet.of(SupplyChainFinanceAgreementDTO.StatusEnum.PENDING_APPROVAL, SupplyChainFinanceAgreementDTO.StatusEnum.ACTIVE, SupplyChainFinanceAgreementDTO.StatusEnum.TERMINATED),
            SupplyChainFinanceAgreementDTO.StatusEnum.PENDING_APPROVAL, EnumSet.of(SupplyChainFinanceAgreementDTO.StatusEnum.DRAFT, SupplyChainFinanceAgreementDTO.StatusEnum.ACTIVE, SupplyChainFinanceAgreementDTO.StatusEnum.TERMINATED),
            SupplyChainFinanceAgreementDTO.StatusEnum.ACTIVE, EnumSet.of(SupplyChainFinanceAgreementDTO.StatusEnum.SUSPENDED, SupplyChainFinanceAgreementDTO.StatusEnum.CLOSED, SupplyChainFinanceAgreementDTO.StatusEnum.TERMINATED),
            SupplyChainFinanceAgreementDTO.StatusEnum.SUSPENDED, EnumSet.of(SupplyChainFinanceAgreementDTO.StatusEnum.ACTIVE, SupplyChainFinanceAgreementDTO.StatusEnum.CLOSED, SupplyChainFinanceAgreementDTO.StatusEnum.TERMINATED)
    );

    private SupplyChainFinanceAgreementLifecycle() {
    }

    public static boolean canRemove(SupplyChainFinanceAgreementDTO.StatusEnum status) {
        SupplyChainFinanceAgreementDTO.StatusEnum current = status == null ? SupplyChainFinanceAgreementDTO.StatusEnum.DRAFT : status;
        return current == SupplyChainFinanceAgreementDTO.StatusEnum.DRAFT;
    }

    public static boolean canTransition(SupplyChainFinanceAgreementDTO.StatusEnum from, SupplyChainFinanceAgreementDTO.StatusEnum to) {
        SupplyChainFinanceAgreementDTO.StatusEnum current = from == null ? SupplyChainFinanceAgreementDTO.StatusEnum.DRAFT : from;
        if (to == null) {
            return false;
        }
        if (current == to) {
            return true;
        }
        Set<SupplyChainFinanceAgreementDTO.StatusEnum> next = ALLOWED.get(current);
        return next != null && next.contains(to);
    }
}
