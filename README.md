# CombatMode64

CombatMode64 is a lightweight Spigot plugin that lets players manually toggle a personal combat mode state. It includes simple command controls for checking status, toggling mode, and managing other players when permitted.

## Commands and Subcommands

- `/combatmode` (alias: `/cm`) - Toggles your own combat mode.
- `/combatmode help` - Shows available subcommands.
- `/combatmode toggle [player]` - Toggles combat mode for yourself or a specific player.
- `/combatmode info [player]` - Shows combat mode status for yourself or a specific player.
- `/combatmode reload` - Reloads the plugin configuration.

## Permissions

| Permission | Description |
| --- | --- |
| `combatmode64.combatmode` | Allows using the `/combatmode` command |
| `combatmode64.combatmode.reload` | Allows reloading the CombatMode64 config |
| `combatmode64.combatmode.change` | Allows changing your own combat mode state |
| `combatmode64.combatmode.other.change` | Allows changing another player's combat mode state |
| `combatmode64.combatmode.info` | Allows viewing your own combat mode status |
| `combatmode64.combatmode.other.info` | Allows viewing another player's combat mode status |

## Config Values

| Key | Default | Description |
| --- | --- | --- |
| `allowed_worlds` | `["world"]` | Worlds where combat mode is allowed and enforced. |
| `disable_pvp_by_default` | `true` | Disables PvP by default for players not in combat mode (in allowed worlds). |
| `allow_gamemode_switching` | `true` | Allows gamemode switching while in combat mode; disabling forces survival during combat mode. |
| `combat_status_indicator.enabled` | `true` | Master toggle for nametag combat status suffix display. |
| `combat_status_indicator.in_combat.show` | `true` | Shows the status suffix when a player is in combat mode. |
| `combat_status_indicator.in_combat.text` | `" &c⚔"` | Suffix text shown when in combat mode (`&` color codes supported). |
| `combat_status_indicator.not_in_combat.show` | `false` | Shows the status suffix when a player is not in combat mode. |
| `combat_status_indicator.not_in_combat.text` | `" &a☮"` | Suffix text shown when not in combat mode (`&` color codes supported). |
