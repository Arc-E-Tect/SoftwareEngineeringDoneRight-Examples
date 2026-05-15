package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.web.strategy;

import jakarta.servlet.http.HttpServletRequest;

/**
 * SPI for extracting an API version from an inbound HTTP request.
 *
 * <p>Declare an implementation as a Spring {@code @Bean} to enable version
 * extraction.  If no bean is present, the MFA proxy controller passes the
 * original path through without modification.
 *
 * <p>Two ready-made implementations are provided:
 * <ul>
 *   <li>{@link PathVersionExtractionStrategy} – parses the version from a
 *       path prefix such as {@code /v1/} or {@code /v2/web/}</li>
 *   <li>{@link HeaderVersionExtractionStrategy} – reads the version from a
 *       configurable request header</li>
 * </ul>
 *
 * @see PathVersionExtractionStrategy
 * @see HeaderVersionExtractionStrategy
 */
public interface VersionExtractionStrategy {

    /**
     * Attempts to extract the API version from the given request.
     *
     * @param request the inbound HTTP request
     * @return a {@link VersionedPath} containing the version and the stripped
     *         path, or {@code null} if no version could be determined
     */
    VersionedPath extract(HttpServletRequest request);
}
