package com.arc_e_tect.examples.mfeadapter.domain.spi;

import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.examples.mfeadapter.infrastructure.config.MfeAdapterProperties;

import java.util.Map;

/**
 * {@link RoutingStrategy} that translates inbound path prefixes to
 * downstream path prefixes using a static mapping table.
 *
 * <p>The longest matching entry in the mapping table wins.  If no entry
 * matches, the original path is forwarded unchanged.
 *
 * <p>Enable via a bean declaration:
 * <pre>{@code
 * @Bean
 * public RoutingStrategy routingStrategy(MfeAdapterProperties props) {
 *     return new PathMappingRoutingStrategy(props);
 * }
 * }</pre>
 *
 * <p>Configure path mappings with:
 * <pre>{@code
 * mfe-adapter:
 *   routing:
 *     path-mappings:
 *       "/api/v1": "/api"
 *       "/api/v2": "/v2/api"
 * }</pre>
 */
public class PathMappingRoutingStrategy implements RoutingStrategy {

    private final MfeAdapterProperties mfeAdapterProperties;

    public PathMappingRoutingStrategy(MfeAdapterProperties mfeAdapterProperties) {
        this.mfeAdapterProperties = mfeAdapterProperties;
    }

    @Override
    public String resolvePath(ProxiedRequest request) {
        Map<String, String> mappings = mfeAdapterProperties.getRouting().getPathMappings();
        if (mappings == null || mappings.isEmpty()) {
            return request.getPath();
        }

        String path = request.getPath();
        String bestMatchKey = null;
        for (String key : mappings.keySet()) {
            if (path.startsWith(key)) {
                if (bestMatchKey == null || key.length() > bestMatchKey.length()) {
                    bestMatchKey = key;
                }
            }
        }

        if (bestMatchKey == null) {
            return path;
        }

        String replacement = mappings.get(bestMatchKey);
        String remainder = path.substring(bestMatchKey.length());
        return replacement + remainder;
    }
}
