package com.arc_e_tect.examples.mfeadapter.domain.spi;

import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest;

/**
 * Default {@link RoutingStrategy} that forwards the request path to the
 * microservice without any transformation.
 *
 * <p>This strategy is used automatically when no custom {@link RoutingStrategy}
 * bean is declared in the application context.
 */
public class PassThroughRoutingStrategy implements RoutingStrategy {

    @Override
    public String resolvePath(ProxiedRequest request) {
        return request.getPath();
    }
}
