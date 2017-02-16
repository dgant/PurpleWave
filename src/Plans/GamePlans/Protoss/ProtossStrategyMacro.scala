package Plans.GamePlans.Protoss

import Plans.Generic.Army.DestroyEconomy
import Plans.Generic.Compound.AllParallel
import Plans.Generic.Defense.DefeatWorkerHarass
import Plans.Generic.Macro.Automatic.{BuildGatewayUnitsContinuously, BuildSupplyContinuously, BuildWorkersContinuously}
import Plans.Generic.Macro._
import Types.Buildable.{Buildable, BuildableUnit}
import bwapi.UnitType

class ProtossStrategyMacro extends AllParallel {
  val _buildOrder = List[Buildable] (
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Cybernetics_Core),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway)
  )
  
  children.set(List(
    new BuildSupplyContinuously,
    new BuildWorkersContinuously,
    new BuildGatewayUnitsContinuously,
    new FollowBuildOrder { this.buildables.set(_buildOrder) },
    new DefeatWorkerHarass,
    new DestroyEconomy,
    new GatherGas,
    new GatherMinerals
  ))
}
