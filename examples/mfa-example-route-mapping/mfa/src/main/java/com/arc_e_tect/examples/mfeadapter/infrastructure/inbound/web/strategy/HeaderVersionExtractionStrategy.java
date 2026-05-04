package com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.strategy;

import com.arc_e_tect.examples.mfeadapter.infrastructure.config.MfeAdapterProperties;
import jakarta.servlet.http.HttpServletRequest;

/**
 * {@link VersionExtractionStrategy} that reads the API version from a
 * configurable request header (default: {@code X-API-Version}).
 *
 * <p>The header name is controlled by:
 * <pre>{@code
 * mfe-adapter:
 *   versioning:
 *     version-header: X-API-Version   # header carrying the version
 * }</pre>
 *
 * <p>When the header is present, the original request path is forwarded
 * unchanged — only the version is extracted.  Enable via a bean declaration:
 * <pre>{@code
 * @Bean
 * public VersionExtractionStrategy versionExtractionStrategy(MfeAdapterProperties props) {
 *     return new HeaderVersionExtractionStrategy(props);
 * }
 * }</pre>
 */
public class HeaderVersionExtractionStrategy implements VersionExtractionStrategy {

    private final MfeAdapterProperties mfeAdapterProperties;

    public HeaderVersionExtractionStrategy(MfeAdapterProperties mfeAdapterProperties) {
        this.mfeAdapterProperties = mfeAdapterProperties;
    }

    @Override
    public VersionedPath extract(HttpServletRequest request) {
        String headerName = mfeAdapterProperties.getVersioning().getVersionHeader();
        String version = request.getHeader(headerName);
        if (version == null || version.isBlank()) {
            return null;
        }
        // Path is forwarded as-is; only the version is extracted from the header
        return new VersionedPath(version.trim(), request.getRequestURI());
    }
}
