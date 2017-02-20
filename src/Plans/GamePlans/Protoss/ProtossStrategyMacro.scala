package Plans.GamePlans.Protoss

import Plans.Army.PressureEnemyBase
import Plans.Compound.AllParallel
import Plans.Defense.DefeatWorkerHarass
import Plans.Macro.Automatic._
import Plans.Macro.Build.{FollowBuildOrder, ScheduleBuildOrder}
import Types.Buildable.{Buildable, BuildableUnit, BuildableUpgrade}
import bwapi.{UnitType, UpgradeType}

class ProtossStrategyMacro extends AllParallel {
  
  val _buildOrder = List[Buildable] (
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Cybernetics_Core),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUpgrade(UpgradeType.Singularity_Charge),
    new BuildableUnit(UnitType.Protoss_Forge),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Weapons, 1),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Armor, 1),
    new BuildableUnit(UnitType.Protoss_Citadel_of_Adun),
    new BuildableUpgrade(UpgradeType.Leg_Enhancements),
    new BuildableUnit(UnitType.Protoss_Templar_Archives),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Weapons, 2),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Weapons, 3),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Armor, 1),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Armor, 2),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Armor, 3)
  )
  
  children.set(List(
    new BuildSupplyContinuously,
    new BuildWorkersContinuously,
    new BuildGatewayUnitsContinuously,
    new ScheduleBuildOrder { this.buildables.set(_buildOrder) },
    new FollowBuildOrder,
    new DefeatWorkerHarass,
    new PressureEnemyBase,
    new GatherGas,
    new GatherMinerals
  ))
}
