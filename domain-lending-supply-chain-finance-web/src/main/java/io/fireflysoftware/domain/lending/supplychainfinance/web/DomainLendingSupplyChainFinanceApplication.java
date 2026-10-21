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

package io.fireflysoftware.domain.lending.supplychainfinance.web;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * Entry point for domain-lending-supply-chain-finance.
 */
@SpringBootApplication(scanBasePackages = {
        "io.fireflysoftware.domain.lending.supplychainfinance",
        "org.fireflyframework.web"
})
@EnableWebFlux
@ConfigurationPropertiesScan(basePackages = "io.fireflysoftware.domain.lending.supplychainfinance")
@OpenAPIDefinition(
info = @Info(
        title = "Domain Lending Supply Chain Finance API",
        version = "1.0.0",
        description = "Orchestrates supply chain finance agreements through core-lending-supply-chain-finance. It creates, updates, changes status, and closes agreements through the supply chain finance core. Party, contract, and product identifiers stay UUIDs.",
        contact = @Contact(
                name = "Firefly Software Foundation",
                email = "dev@fireflysoftware.io"
        )
),
servers = {
        @Server(
                url = "https://openplatform.fireflysoftware.io/domain-lending-supply-chain-finance",
                description = "Development Environment"
        ),
        @Server(
                url = "/",
                description = "Local Development Environment"
        )
}
)

public class DomainLendingSupplyChainFinanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DomainLendingSupplyChainFinanceApplication.class, args);
    }
}
