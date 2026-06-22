# RagnarMMO Legacy Compatibility Audit

This file tracks compatibility aliases that must not be removed by intuition.
Before deleting one, run a full grep, document the impact, and prefer a
deprecation window.

## Command Aliases

Registered in `RagnarCommand`:

- `ragnar` -> `/r`
- `stats` -> `/r stats`
- `skills` -> `/r skills`
- `cart` -> `/r cart`
- `party` -> `/r party`
- `memo` -> `/r memo`
- `lv` -> `/r lv`
- `job` -> `/r job`
- `exp` -> `/r exp`
- `set` -> `/r set`
- `unlock` -> `/r unlock`
- `reset` -> `/r reset`
- `debug` -> `/r debug`
- `admin` -> `/r admin`
- `pc` -> `/r party chat`

These are player-facing command shortcuts. Keep them unless a release note and
migration path exists.

Current single-mod coverage:

- The `jobs` package no longer registers direct player-facing job commands.
- Job viewing, job change, skill leveling and skill hotbar assignment are handled through UI/packets.
- Future `/jobs` or debug commands should be purpose-built instead of restoring the old smoke-test commands.

## Job Change Compatibility

Legacy `PacketChangeJob` behavior is represented server-side by
`JobChangeService` in the `jobs` package.

Preserved rules for the current phase:

- Novice can only change to first classes.
- Novice must reach the configured Novice Job Lv cap.
- `ragnarmmo:basic_skill` must be Lv 9.
- Job Skill Points must be fully spent before changing class.
- Job ID changes to `ragnarmmo:<first_class>`, Job Lv resets to 1, Job EXP resets to 0.
- Non-Novice skill levels are cleared during the Novice-to-first-class transition.

Second-class promotion remains intentionally deferred.

## Mixin Field Aliases

`MerchantOfferMixin` uses obfuscated and named field aliases:

- `f_45310_`, `baseCostA`
- `f_45311_`, `costB`
- `f_45312_`, `result`

These are runtime compatibility aliases for different mapping environments.
Do not remove either side without checking client and dedicated server startup.

## Skill Level Data Aliases

`SkillDefinition#getResourceCost` accepts these level-data keys:

- `resource_cost`
- `sp_cost`
- `mana_cost`

`sp_cost` is the canonical authored key for current JSON skill data. The other
keys remain compatibility aliases for older or external data packs.
