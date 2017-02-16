package Plans.GamePlans.Protoss

import Plans.Generic.Army.DestroyEconomy
import Plans.Generic.Compound.AllParallel
import Plans.Generic.Defense.DefeatWorkerHarass
import Plans.Generic.Macro.{FollowBuildOrder, GatherMinerals}
import Types.Buildable.{Buildable, BuildableUnit}
import bwapi.UnitType

class ProtossStrategyMacro extends AllParallel {
  val _buildOrder = List[Buildable] (
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Pylon),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Pylon),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Pylon),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Pylon),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Pylon),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Pylon),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Zealot),
    new BuildableUnit(UnitType.Protoss_Pylon),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe)
  )
  
  children.set(List(
    new FollowBuildOrder { this.buildables.set(_buildOrder) },
    new DefeatWorkerHarass,
    new DestroyEconomy,
    new GatherMinerals
  ))
}
