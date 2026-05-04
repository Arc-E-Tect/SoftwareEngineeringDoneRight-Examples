# MFA Example - Payload Shaping implementation

These are the instructions for the MFA example in which the configuration is slightly more complex then as simple as possible.
The instructions use the instructions from the Vanilla MFA example as a basis.
In fact, it provides the same functionality as the Vanilla MFA example, with the only difference being that the MFE in the `blue-needle` project is in Dutch and the APIs provided by the MFA are in Dutch as well.
This will require the MFA to translate the incoming requests from the MFE, which are in Dutch, to the requests that the MS expects, which are in English.

This example shows how to implement payload shaping in the MFA.

The implementation must therefore be based on the Vanilla MFA example, and all changes made to the Vanilla MFA example with respect to the MFA framework found in `mfe-adapter` must be applied to the Payload-Shaping MFA implementation too.

The instructions for the Vanilla MFA example can be found [here](./vanilla-mfa-example.md).

The example must be implemented in `examples/mfa-example-payload-shaping` following the same structure as the Vanilla MFA example.
The UI must be translated to Dutch, as must the APIs exposed by the MFA.