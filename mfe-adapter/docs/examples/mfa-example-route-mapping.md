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

