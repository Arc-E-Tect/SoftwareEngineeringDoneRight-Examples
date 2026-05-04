package com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.http;

import com.arc_e_tect.examples.mfeadapter.domain.model.InnerToken;
import com.arc_e_tect.examples.mfeadapter.domain.model.UserToken;
import com.arc_e_tect.examples.mfeadapter.domain.port.outbound.InnerTokenServicePort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * No-op {@link InnerTokenServicePort} used in the route-mapping (no-auth) profile.
 *
 * <p>In the route-mapping example there is no Authorization Service, so no token
 * swap is performed.  This adapter always returns {@code null}, which the
 * proxy controller treats as "no inner token – send request without an
 * {@code Authorization} header".
 */
@Profile("route-mapping")
@Component
public class RouteMappingInnerTokenServiceAdapter implements InnerTokenServicePort {

    @Override
    public InnerToken swapForInnerToken(UserToken userToken) {
        // No authorization service in the route-mapping example – return null to skip the
        // Authorization header on outbound MS requests.
        return null;
    }
}
