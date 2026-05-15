package com.arc_e_tect.examples.mfeadapter.domain.spi;

import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest;

/**
 * SPI for resolving the forwarding URI path for a proxied request.
 *
 * <p>Declare an implementation as a Spring {@code @Bean} to customise how
 * inbound paths are translated before being forwarded to the microservice.
 * If no bean is present, the {@link PassThroughRoutingStrategy} is used as the
 * default (path forwarded unchanged).
 *
 * <p>Three ready-made implementations are provided:
 * <ul>
 *   <li>{@link PassThroughRoutingStrategy} – no path transformation (default)</li>
 *   <li>{@link PrefixStrippingRoutingStrategy} – strips the microservice
 *       context-path prefix before forwarding</li>
 *   <li>{@link PathMappingRoutingStrategy} – replaces path prefixes according
 *       to a static mapping table</li>
 * </ul>
 *
 * @see PassThroughRoutingStrategy
 * @see PrefixStrippingRoutingStrategy
 * @see PathMappingRoutingStrategy
 */
public interface RoutingStrategy {

    /**
     * Resolves the forwarding path (without base URL) for the given request.
     *
     * @param request the proxied request domain object
     * @return the path to use when forwarding to the microservice, never {@code null}
     */
    String resolvePath(ProxiedRequest request);
}
