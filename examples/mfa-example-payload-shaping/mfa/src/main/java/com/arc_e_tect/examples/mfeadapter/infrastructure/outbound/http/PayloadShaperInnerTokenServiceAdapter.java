package com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.http;

import com.arc_e_tect.examples.mfeadapter.domain.model.InnerToken;
import com.arc_e_tect.examples.mfeadapter.domain.model.UserToken;
import com.arc_e_tect.examples.mfeadapter.domain.port.outbound.InnerTokenServicePort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * No-op {@link InnerTokenServicePort} used in the payload-shaper (no-auth) profile.
 *
 * <p>In the payload-shaper example there is no Authorization Service, so no token
 * swap is performed.  This adapter always returns {@code null}, which the
 * proxy controller treats as "no inner token – send request without an
 * {@code Authorization} header".
 */
@Profile("payload-shaper")
@Component
public class PayloadShaperInnerTokenServiceAdapter implements InnerTokenServicePort {

    @Override
    public InnerToken swapForInnerToken(UserToken userToken) {
        // No authorization service in the payload-shaper example – return null to skip the
        // Authorization header on outbound MS requests.
        return null;
    }
}
