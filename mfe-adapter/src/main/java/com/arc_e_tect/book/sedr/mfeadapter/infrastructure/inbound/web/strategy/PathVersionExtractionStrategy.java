package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.web.strategy;

import jakarta.servlet.http.HttpServletRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link VersionExtractionStrategy} that parses the API version from the
 * request path prefix.
 *
 * <p>Recognised formats:
 * <ul>
 *   <li>{@code /v{n}/...} – version only; e.g. {@code /v1/customers}</li>
 *   <li>{@code /v{n}/{channel}/...} – version + channel; e.g.
 *       {@code /v2/web/customers}</li>
 * </ul>
 *
 * <p>Both the version segment and the optional channel segment are stripped
 * from the forwarded path.  For example, {@code /v2/web/customers?page=1}
 * yields version {@code "2"} and stripped path {@code /customers}.
 *
 * <p>Enable via a bean declaration:
 * <pre>{@code
 * @Bean
 * public VersionExtractionStrategy versionExtractionStrategy() {
 *     return new PathVersionExtractionStrategy();
 * }
 * }</pre>
 */
public class PathVersionExtractionStrategy implements VersionExtractionStrategy {

    /**
     * Matches {@code /v{digits}} optionally followed by {@code /{channel}}
     * where channel is a single path segment that does NOT start with a digit
     * (to distinguish it from a resource ID).
     */
    private static final Pattern VERSION_PATTERN =
            Pattern.compile("^/v(\\d+)(?:/([a-zA-Z][^/]*))?(/.*)?$");

    @Override
    public VersionedPath extract(HttpServletRequest request) {
        String path = request.getRequestURI();
        Matcher m = VERSION_PATTERN.matcher(path);
        if (!m.matches()) {
            return null;
        }
        String version = m.group(1);
        String remainder = m.group(3);  // everything after /v{n}[/{channel}]
        String strippedPath = (remainder != null && !remainder.isEmpty()) ? remainder : "/";
        return new VersionedPath(version, strippedPath);
    }
}
