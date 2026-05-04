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

## Clarifications

The following clarifications apply to this example and supersede any ambiguity in the instructions above.

### What "Dutch" means

Only field **names** (JSON keys) are translated between Dutch and English.
Field **values** (e.g. the relationship type string `parent`) pass through unchanged.
URL paths remain in English (e.g. `/v1/familyties/persons`).

### Dutch ↔ English field-name mapping

| Dutch (MFE / MFA surface) | English (MS surface) |
|---|---|
| `voornaam` | `firstName` |
| `achternaam` | `lastName` |
| `vanVoornaam` | `fromFirstName` |
| `vanAchternaam` | `fromLastName` |
| `naarVoornaam` | `toFirstName` |
| `naarAchternaam` | `toLastName` |
| `soort` | `type` |
| `vanPersoon` | `fromPerson` |
| `naarPersoon` | `toPerson` |

### Spring profile

The active Spring profile for this example is `payload-shaper` (not `vanilla`).
All payload-shaping beans are guarded with `@Profile("payload-shaper")`.
Beans that must not activate under this profile are annotated `@Profile("!payload-shaper")`.

### MFA SPI injection

The translation is implemented exclusively via two new SPI beans registered under the `payload-shaper` profile.
No MFA framework code is modified:

- `DutchToEnglishRequestTransformer` — implements `RequestTransformer`; translates inbound request bodies from Dutch field names to English.
- `EnglishToDutchResponseTransformer` — implements `ResponseTransformer`; translates outbound response bodies from English field names to Dutch.

Both beans are declared in `PayloadShapingConfiguration` and receive the auto-configured `ObjectMapper` via constructor injection.

### MS is unchanged

The microservice (MS) module is a verbatim copy of the vanilla-mfa MS.
Because the MFA transformers ensure the MS always receives and returns English field names, no changes to the MS are required.

### Derivation guide

A step-by-step explanation of every change made relative to the vanilla-mfa example is available in `examples/mfa-example-payload-shaping/docs/derivation-guide.adoc`.