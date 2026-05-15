# MFA Example - Security implementation

These are the instructions for the what is probably the most complex example of the MFA examples.
The instructions use the instructions from the Vanilla MFA, the Payload Shaping, and the Route Mapping examples as a basis.
In fact, it provides the same functionality as the Route Mapping MFA example, with a ton of security applied.
This means that the APIs exposed by the MFA and the MS remain the same as in the Route Mapping example.

The MFA in this example is a security-focused implementation of the Route Mapping MFA example.

- It checks for a valid API key header in the request.
  This means that the Blue Needle project must include the header that would typically be added by an API Gateway as there is no API Gateway in this example.
- It checks for a valid session cookie in the request.
  This means that the Blue Needle project must include the session cookie in the request and it is perfectly fine when the session cookie is purely syntesized.
  There is no need to use a real session cookie.
- It will swap the session cookie for a user token by calling the IdP service to retrieve the user token based on the session cookie.
  There is no IdP service in this example, so it must be implemented using WireMock that provides an API that would be typically provided by an IdP service.
  Make one up, there is no need to use a real IdP service, or have an API that reflects a real IdP service API, this is just to illustrate that the MFA can call an IdP service to retrieve a user token based on the session cookie.
- It will swap the user token for an inner token with roles defined as claims.
  There is no authorization service in this example, so it must be implemented using WireMock that provides an API that would be typically provided by an authorization service.
  Make one up, there is no need to use a real authorization service, or have an API that reflects a real authorization service API, this is just to illustrate that the MFA can call an authorization service to retrieve an inner token with roles defined as claims based on the user token.
  The MFA will then use the inner token to call the MS and will have a request header with the API key scope and the inner token.

All of the above is done by the MFA, and is purely intended to illustrate the MFA framework.
This can be done by injecting the relevant functionality into the MFA using the MFA framework and by configuring the MFA to use the MFA framework.

This example shows how to implement security in the MFA.

The implementation must therefore be based on the Route Mapping MFA example, and all changes made to the Route Mapping MFA example with respect to the MFA framework found in `mfe-adapter` must be applied to the Security MFA implementation too.

The instructions for the Vanilla MFA example can be found [here](./mfa-example-vanilla.md).
The instructions for the Payload Shaping MFA example can be found [here](./mfa-example-payload-shaping.md).
The instructions for the Route Mapping MFA example can be found [here](./mfa-example-route-mapping.md).

The example must be implemented in `examples/mfa-example-security` following the same structure as the other MFA examples.

It is imperative that the guides in `./mfe-adapter/docs` are used as reference.