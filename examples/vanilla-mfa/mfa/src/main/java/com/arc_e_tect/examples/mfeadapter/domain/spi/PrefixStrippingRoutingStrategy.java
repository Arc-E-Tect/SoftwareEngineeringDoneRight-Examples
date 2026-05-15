package com.arc_e_tect.examples.mfeadapter.domain.spi;

import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.examples.mfeadapter.infrastructure.config.MfeAdapterProperties;

/**
 * {@link RoutingStrategy} that strips the configurable microservice
 * context-path prefix from the forwarded path.
 *
 * <p>For example, if the MFA is mounted at {@code /api} and the microservice
 * root is {@code /}, a request for {@code /api/customers} will be forwarded
 * as {@code /customers}.
 *
 * <p>Enable via a bean declaration:
 * <pre>{@code
 * @Bean
 * public RoutingStrategy routingStrategy(MfeAdapterProperties props) {
 *     return new PrefixStrippingRoutingStrategy(props);
 * }
 * }</pre>
 *
 * <p>Configure the prefix to strip with:
 * <pre>{@code
 * mfe-adapter:
 *   microservice:
 *     context-path: /api   # default
 * }</pre>
 */
public class PrefixStrippingRoutingStrategy implements RoutingStrategy {

    private final MfeAdapterProperties mfeAdapterProperties;

    public PrefixStrippingRoutingStrategy(MfeAdapterProperties mfeAdapterProperties) {
        this.mfeAdapterProperties = mfeAdapterProperties;
    }

    @Override
    public String resolvePath(ProxiedRequest request) {
        String contextPath = mfeAdapterProperties.getMicroservice().getContextPath();
        String path = request.getPath();
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            String stripped = path.substring(contextPath.length());
            return stripped.isEmpty() ? "/" : stripped;
        }
        return path;
    }
}
