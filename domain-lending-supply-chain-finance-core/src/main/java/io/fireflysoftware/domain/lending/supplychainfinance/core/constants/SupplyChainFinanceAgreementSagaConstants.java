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

package io.fireflysoftware.domain.lending.supplychainfinance.core.constants;

/**
 * Names shared by the supply chain finance agreement sagas, their steps, and the saga engine.
 */
public final class SupplyChainFinanceAgreementSagaConstants {

    public static final String SAGA_REGISTER = "RegisterSupplyChainFinanceAgreementSaga";
    public static final String SAGA_UPDATE_STATUS = "UpdateSupplyChainFinanceAgreementStatusSaga";
    public static final String SAGA_UPDATE_RECORD = "UpdateSupplyChainFinanceAgreementSaga";

    public static final String STEP_REGISTER = "registerSupplyChainFinanceAgreement";
    public static final String STEP_RETRIEVE = "retrieveSupplyChainFinanceAgreement";
    public static final String STEP_UPDATE_STATUS = "updateSupplyChainFinanceAgreementStatus";
    public static final String STEP_RETRIEVE_FOR_UPDATE = "retrieveSupplyChainFinanceAgreementForUpdate";
    public static final String STEP_UPDATE_RECORD = "updateSupplyChainFinanceAgreement";

    public static final String COMPENSATE_REMOVE = "removeSupplyChainFinanceAgreement";
    public static final String COMPENSATE_RESTORE = "restoreSupplyChainFinanceAgreementStatus";
    public static final String COMPENSATE_RESTORE_RECORD = "restoreSupplyChainFinanceAgreement";

    public static final String EVENT_REGISTERED = "supply-chain-finance-agreements.registered";
    public static final String EVENT_RETRIEVED = "supply-chain-finance-agreements.retrieved";
    public static final String EVENT_STATUS_UPDATED = "supply-chain-finance-agreements.status.updated";
    public static final String EVENT_RECORD_UPDATED = "supply-chain-finance-agreements.updated";

    public static final String CTX_RECORD = "supply-chain-finance-agreementsRecord";
    public static final String CTX_PREVIOUS_STATUS = "previousSupplyChainFinanceAgreementStatus";
    public static final String CTX_PREVIOUS_RECORD = "previousSupplyChainFinanceAgreementRecord";

    private SupplyChainFinanceAgreementSagaConstants() {
    }
}
