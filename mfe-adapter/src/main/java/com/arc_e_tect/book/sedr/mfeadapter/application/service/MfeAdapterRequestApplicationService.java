package com.arc_e_tect.book.sedr.mfeadapter.application.service;

import com.arc_e_tect.book.sedr.mfeadapter.application.exception.AuthenticationException;
import com.arc_e_tect.book.sedr.mfeadapter.application.exception.ValidationException;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.InnerToken;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ProxiedResponse;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.Session;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.UserToken;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ValidationResult;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.inbound.HandleRequestUseCase;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.outbound.InnerTokenServicePort;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.outbound.MicroserviceClientPort;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.outbound.SessionStorePort;
import com.arc_e_tect.book.sedr.mfeadapter.domain.spi.RequestTransformer;
import com.arc_e_tect.book.sedr.mfeadapter.domain.spi.RequestValidator;
import com.arc_e_tect.book.sedr.mfeadapter.domain.spi.ResponseTransformer;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.config.MfeAdapterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Application service that orchestrates the MFA's core request-handling flow:
 * <ol>
 *   <li>Validate the session and retrieve the associated user token.</li>
 *   <li>Obtain an inner token from SecService (skipped when
 *       {@code mfe-adapter.authorization-service.required=false}).</li>
 *   <li>Run all applicable {@link RequestValidator}s.</li>
 *   <li>Run all applicable {@link RequestTransformer}s.</li>
 *   <li>Forward the request to the microservice.</li>
 *   <li>Run all applicable {@link ResponseTransformer}s.</li>
 *   <li>Return the (possibly transformed) response.</li>
 * </ol>
 */
@Service
public class MfeAdapterRequestApplicationService implements HandleRequestUseCase {

    private static final Logger log = LoggerFactory.getLogger(MfeAdapterRequestApplicationService.class);

    private final SessionStorePort sessionStore;
    private final InnerTokenServicePort innerTokenService;
    private final MicroserviceClientPort microserviceClient;
    private final MfeAdapterProperties mfeAdapterProperties;
    private final List<RequestValidator> requestValidators;
    private final List<RequestTransformer> requestTransformers;
    private final List<ResponseTransformer> responseTransformers;

    public MfeAdapterRequestApplicationService(
            SessionStorePort sessionStore,
            InnerTokenServicePort innerTokenService,
            MicroserviceClientPort microserviceClient,
            MfeAdapterProperties mfeAdapterProperties,
            List<RequestValidator> requestValidators,
            List<RequestTransformer> requestTransformers,
            List<ResponseTransformer> responseTransformers) {
        this.sessionStore = sessionStore;
        this.innerTokenService = innerTokenService;
        this.microserviceClient = microserviceClient;
        this.mfeAdapterProperties = mfeAdapterProperties;
        this.requestValidators = requestValidators;
        this.requestTransformers = requestTransformers;
        this.responseTransformers = responseTransformers;
    }

    @Override
    public ProxiedResponse handle(ProxiedRequest request) {
        log.debug("Handling proxied request: {} {}", request.getMethod(), request.getPath());

        // 1. Retrieve and validate session
        Session session = sessionStore.findById(request.getSessionId())
                .filter(s -> !s.isExpired())
                .orElseThrow(() -> new AuthenticationException(
                        "No valid session for id: " + request.getSessionId()));

        UserToken userToken = session.userToken();

        // 2. Swap user token for inner token (SecService) — conditional on config
        InnerToken innerToken = null;
        if (mfeAdapterProperties.getAuthorizationService().isRequired()) {
            innerToken = innerTokenService.swapForInnerToken(userToken);
        } else {
            log.debug("SecService call skipped – mfe-adapter.authorization-service.required=false");
        }

        // 3. Run validators
        validate(request);

        // 4. Apply request transformers
        ProxiedRequest transformedRequest = applyRequestTransformers(request);

        // 5. Forward to microservice
        ProxiedResponse msResponse = microserviceClient.forward(transformedRequest, innerToken);

        // 6. Apply response transformers
        return applyResponseTransformers(msResponse, request);
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private void validate(ProxiedRequest request) {
        List<String> errors = new ArrayList<>();
        for (RequestValidator validator : requestValidators) {
            if (validator.supports(request)) {
                ValidationResult result = validator.validate(request);
                if (!result.valid()) {
                    errors.addAll(result.errors());
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private ProxiedRequest applyRequestTransformers(ProxiedRequest request) {
        ProxiedRequest current = request;
        for (RequestTransformer transformer : requestTransformers) {
            if (transformer.supports(current)) {
                current = transformer.transform(current);
            }
        }
        return current;
    }

    private ProxiedResponse applyResponseTransformers(ProxiedResponse response, ProxiedRequest request) {
        ProxiedResponse current = response;
        for (ResponseTransformer transformer : responseTransformers) {
            if (transformer.supports(current)) {
                current = transformer.transform(current, request);
            }
        }
        return current;
    }
}
