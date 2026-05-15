# MFA Example - Route Mapping implementation

These are the instructions for the MFA example in which the configuration is slightly more complex then an implementation with just request/response shaping.
The instructions use the instructions from the Vanilla MFA and the Payload Shaping examples as a basis.
In fact, it provides the same functionality as the Payload Shaping MFA example, with the only difference being that the MFE in the `blue-needle` project is including a prefix in the paths to the MFA.
The prefix being `dutch` while the APIs provided by the MS remain the same.
This will require the MFA to route the requests coming in on the 'Dutch' endpoints to the language agnostic MS endpoints.
The request and responses between MFE and MFA are already in Dutch, the MS is still in English as it is from the Payload Shaping example.

This example shows how to implement routing in the MFA.

The implementation must therefore be based on the Payload Shaping MFA example, and all changes made to the Payload-Shaping MFA example with respect to the MFA framework found in `mfe-adapter` must be applied to the Route Mapper MFA implementation too.

The instructions for the Vanilla MFA example can be found [here](./mfa-example-vanilla.md).
The instructions for the Payload Shaping MFA example can be found [here](./mfa-example-payload-shaping.md).

The example must be implemented in `examples/mfa-example-route-mapping` following the same structure as the other MFA examples.
The UI must be translated to Dutch, as must the APIs exposed by the MFA.
The APIs exposed by the MFA are `/dutch/persons`,for example, and must be mapped to the corresponding APIs in the MS.

## Clarifications

The following clarifications apply to this example and supersede any ambiguity in the instructions above.

### Active Spring profile

The MFA must be started with `spring.profiles.active=route-mapping`.
The Docker Compose service for the MFA sets `SPRING_PROFILES_ACTIVE: route-mapping`.
The Gradle test task also sets `systemProperty 'spring.profiles.active', 'route-mapping'`.

### URL structure

All MFE → MFA paths follow the pattern `/v{n}/dutch/{resource}`, for example:

| MFE calls (MFA endpoint)                              | MFA forwards to MS                      |
|-------------------------------------------------------|-----------------------------------------|
| `GET  /v1/dutch/familyties/lastnames/{lastName}`      | `GET  /v1/familyties/lastnames/{lastName}` |
| `POST /v1/dutch/familyties`                           | `POST /v1/familyties`                   |
| `DELETE /v1/dutch/familyties/lastnames/{lastName}?firstname={firstName}` | `DELETE /v1/familyties/lastnames/{lastName}?firstname={firstName}` |
| `POST /v1/dutch/familyties/relationships`             | `POST /v1/familyties/relationships`     |
| `GET  /v1/dutch/familyties/relationships/{type}/lastnames/{lastName}` | `GET  /v1/familyties/relationships/{type}/lastnames/{lastName}` |

The `dutch` segment is the only structural difference between the MFA-facing and MS-facing paths.

### Routing strategy: `RegexRoutingStrategy`

The routing strategy is example-scoped (not part of the `mfe-adapter` library).
It lives in `examples/mfa-example-route-mapping/mfa/src/main/java/.../domain/spi/RegexRoutingStrategy.java`.

Configuration properties are bound via the example-scoped `RouteMappingProperties` class
(`@ConfigurationProperties(prefix = "route-mapping")`).
The `application-route-mapping.yml` file sets:

```yaml
route-mapping:
  routing:
    path-pattern:     "^(/v\\d+)/dutch(/.+)$"
    path-replacement: "$1$2"
```

`RegexRoutingStrategy.resolvePath()` compiles the pattern at construction time and calls
`Pattern.matcher(path).replaceFirst(replacement)` to strip the `/dutch` infix.
Paths that do not match the pattern are returned unchanged.

### `@Profile` guards on library beans

All production beans in the `mfe-adapter` library that are active in the *vanilla* profile
but must be excluded from the route-mapping profile carry the guard:

```java
@Profile("!payload-shaper && !route-mapping")
```

This prevents the default pass-through infrastructure from conflicting with the
example-specific beans registered by `RouteMappingConfiguration`.

### Dockerfile: JAR artifact name

The Dockerfile at `examples/mfa-example-route-mapping/mfa/Dockerfile` must reference:

```dockerfile
COPY --from=builder /workspace/build/libs/route-mapping-mfa-*.jar app.jar
```

The user and group are both `route-mapping-mfa`.
The ENTRYPOINT includes `-Dspring.profiles.active=route-mapping` so the correct
profile is active even when the image is run without explicit env-var overrides.

### Microservice (MS): no changes required

The MS used in this example is an unmodified copy of the payload-shaping MS.
Its source lives in `examples/mfa-example-route-mapping/ms/`.
No routing or translation logic needs to be added to the MS.

### Hop-by-hop header forwarding (library fix)

The MS returns `Transfer-Encoding: chunked` in its responses.
`MicroserviceRestClientAdapter` must **not** forward hop-by-hop headers from the upstream
MS response to the caller.  Forwarding `Transfer-Encoding` causes Spring Boot to emit a
second `Transfer-Encoding: chunked` header, which breaks client-side chunk parsing and
results in an empty response body.

The fix is in the library's `MicroserviceRestClientAdapter.forward()` method:
response headers are filtered through `isForwardableResponseHeader(name)` before being
placed in the `ProxiedResponse`.  The filtered headers are:
`transfer-encoding`, `content-length`, `connection`, `keep-alive`, `te`, `trailer`, `upgrade`.

This fix must also be applied to the local copy of `MicroserviceRestClientAdapter` that
lives inside each example's `mfa/` subproject.
