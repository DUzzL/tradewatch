# TradeWatch

TradeWatch is a client-side Fabric mod for Minecraft **26.1**, **26.1.1**, and **26.1.2** that finds librarian enchanted-book trades you actually want. It never rerolls, clicks trades, interacts with blocks, breaks lecterns, or automates villagers.

## Requirements

Minecraft 26.1, 26.1.1, or 26.1.2, Fabric Loader 0.19.3 or newer, and the matching Fabric API release are required. One TradeWatch JAR supports all three versions. Mod Menu is optional.

## Building

The single distributable JAR is compiled against Minecraft 26.1, the oldest supported API level:

```powershell
.\gradlew.bat build
```

Install the Fabric API release matching the selected Minecraft version: `0.145.1+26.1`, `0.145.4+26.1.1`, or `0.154.2+26.1.2`.

## Using TradeWatch

Open the configuration screen with the default **G** key (rebindable in Minecraft Controls), `/tradewatch`, or Mod Menu's **Configure** button. Search by translated enchantment name (`eff`, `mend`) or registry ID (`minecraft:efficiency`), select an enchantment, enable its individual levels, and set each level's independent maximum price with a 1–64 emerald slider. Changes are saved immediately to `config/tradewatch.json`.

Only enchanted-book offers in the villager merchant screen are checked; other results are ignored. TradeWatch uses the actual current emerald stack shown by the offer, including discounts, and ignores the required Book input. Matching rows are green, a vanilla ping is played once per opened merchant screen, and a non-blocking center-screen panel provides **Keep Watching** and **Stop watching <enchantment>** actions. Stopping removes all watched levels for that enchantment.

Wishlist entries use Minecraft registry identifiers and independent prices per level. For example, Efficiency IV can be watched for 15 emeralds while Efficiency V is watched for 32. Invalid or unavailable registry entries are safely removed after registry synchronization.

## Commands

```
/tradewatch
/tradewatch add minecraft:efficiency 5 price:32
/tradewatch remove minecraft:efficiency
/tradewatch remove minecraft:efficiency 5
/tradewatch list
/tradewatch clear
/tradewatch clear confirm
```

`add` validates the runtime registry identifier, supported level, and 1–64 price. The exact `price:32` syntax is required. `remove <enchantment>` removes every watched level for that enchantment; `remove <enchantment> <level>` only removes that level. `clear` requires the explicit `confirm` literal. Prices are configured per enchantment level, not globally.
