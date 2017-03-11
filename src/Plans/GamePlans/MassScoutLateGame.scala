package Plans.GamePlans

import Types.Buildable.{Buildable, BuildableUnit, BuildableUpgrade}
import bwapi.{UnitType, UpgradeType}

object MassScoutLateGame {
  val build = List[Buildable] (
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Stargate),
    new BuildableUnit(UnitType.Protoss_Forge),
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUpgrade(UpgradeType.Protoss_Air_Weapons, 1),
    new BuildableUnit(UnitType.Protoss_Stargate),
    new BuildableUnit(UnitType.Protoss_Stargate),
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUpgrade(UpgradeType.Protoss_Air_Armor, 1),
    new BuildableUnit(UnitType.Protoss_Stargate),
    new BuildableUnit(UnitType.Protoss_Stargate),
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Fleet_Beacon),
    new BuildableUpgrade(UpgradeType.Protoss_Air_Weapons, 2),
    new BuildableUpgrade(UpgradeType.Gravitic_Thrusters),
    new BuildableUpgrade(UpgradeType.Protoss_Air_Armor, 2),
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Stargate),
    new BuildableUnit(UnitType.Protoss_Stargate),
    new BuildableUnit(UnitType.Protoss_Stargate),
    new BuildableUnit(UnitType.Protoss_Stargate)
  )
}
