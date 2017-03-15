package Planning.Plans.GamePlans

import Macro.BuildRequests.{BuildRequest, RequestUnitAnotherOne, RequestUpgrade}
import bwapi.{UnitType, UpgradeType}

object MassScoutLateGame {
  val build = List[BuildRequest] (
    new RequestUnitAnotherOne(UnitType.Protoss_Assimilator),
    new RequestUnitAnotherOne(UnitType.Protoss_Stargate),
    new RequestUnitAnotherOne(UnitType.Protoss_Stargate),
    new RequestUnitAnotherOne(UnitType.Protoss_Nexus),
    new RequestUnitAnotherOne(UnitType.Protoss_Assimilator),
    new RequestUpgrade(UpgradeType.Protoss_Air_Weapons, 1),
    new RequestUnitAnotherOne(UnitType.Protoss_Stargate),
    new RequestUnitAnotherOne(UnitType.Protoss_Stargate),
    new RequestUnitAnotherOne(UnitType.Protoss_Nexus),
    new RequestUnitAnotherOne(UnitType.Protoss_Assimilator),
    new RequestUpgrade(UpgradeType.Protoss_Air_Armor, 1),
    new RequestUnitAnotherOne(UnitType.Protoss_Stargate),
    new RequestUnitAnotherOne(UnitType.Protoss_Stargate),
    new RequestUnitAnotherOne(UnitType.Protoss_Nexus),
    new RequestUnitAnotherOne(UnitType.Protoss_Assimilator),
    new RequestUnitAnotherOne(UnitType.Protoss_Fleet_Beacon),
    new RequestUpgrade(UpgradeType.Protoss_Air_Weapons, 2),
    new RequestUpgrade(UpgradeType.Gravitic_Thrusters),
    new RequestUpgrade(UpgradeType.Protoss_Air_Armor, 2),
    new RequestUnitAnotherOne(UnitType.Protoss_Nexus),
    new RequestUnitAnotherOne(UnitType.Protoss_Assimilator),
    new RequestUnitAnotherOne(UnitType.Protoss_Gateway),
    new RequestUnitAnotherOne(UnitType.Protoss_Gateway),
    new RequestUnitAnotherOne(UnitType.Protoss_Stargate),
    new RequestUnitAnotherOne(UnitType.Protoss_Stargate)
  )
}
