package Plans.GamePlans

import Types.Buildable.{Buildable, BuildableUnit, BuildableUpgrade}
import bwapi.{UnitType, UpgradeType}

object MemeBuild {
  val initialBuild = List[Buildable] (
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Cybernetics_Core),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Robotics_Facility),
    new BuildableUnit(UnitType.Protoss_Robotics_Support_Bay)
  )
  
  val laterBuild = List[Buildable] (
    
  
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Citadel_of_Adun),
    new BuildableUpgrade(UpgradeType.Singularity_Charge),
    new BuildableUnit(UnitType.Protoss_Templar_Archives),
  
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    
    new BuildableUpgrade(UpgradeType.Leg_Enhancements),
    new BuildableUpgrade(UpgradeType.Scarab_Damage),
    
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Stargate),
    new BuildableUnit(UnitType.Protoss_Fleet_Beacon),
    new BuildableUnit(UnitType.Protoss_Stargate),
    new BuildableUpgrade(UpgradeType.Carrier_Capacity),
    new BuildableUpgrade(UpgradeType.Protoss_Air_Weapons, 1),
    
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Stargate),
    new BuildableUnit(UnitType.Protoss_Robotics_Facility),
    new BuildableUpgrade(UpgradeType.Protoss_Air_Armor, 1),
    
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Stargate),
    new BuildableUnit(UnitType.Protoss_Robotics_Facility),
    new BuildableUnit(UnitType.Protoss_Arbiter_Tribunal),
    new BuildableUpgrade(UpgradeType.Protoss_Air_Weapons, 2),
    
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Stargate),
    
    
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Stargate),
  
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUpgrade(UpgradeType.Protoss_Air_Weapons, 3),
    new BuildableUpgrade(UpgradeType.Protoss_Air_Armor, 2),
    new BuildableUpgrade(UpgradeType.Protoss_Air_Armor, 3),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Weapons, 2),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Weapons, 3),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Armor, 2),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Armor, 3),
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Stargate),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Stargate),
    new BuildableUnit(UnitType.Protoss_Gateway)
  )
}
