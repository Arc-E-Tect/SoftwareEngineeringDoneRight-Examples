package com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.strategy;

/**
 * Immutable value object that holds the result of version extraction from an
 * inbound request path.
 *
 * @param version      the extracted API version string (e.g. {@code "1"}, {@code "2"})
 * @param strippedPath the request path with the version prefix (and any channel segment)
 *                     removed, ready to be forwarded to the microservice
 */
public record VersionedPath(String version, String strippedPath) {
}
