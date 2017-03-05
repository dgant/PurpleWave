package Plans.GamePlans

import Plans.Army.{Attack, DefendChoke}
import Plans.Compound.{AllParallel, IfThenElse}
import Plans.Defense.DefeatWorkerHarass
import Plans.Information.ScoutAt
import Plans.Macro.Automatic._
import Plans.Macro.Build.{FollowBuildOrder, ScheduleBuildOrder}
import Plans.Macro.UnitCount.UnitCountAtLeast
import Strategies.UnitMatchers.UnitMatchWarriors
import Types.Buildable.{Buildable, BuildableUnit, BuildableUpgrade}
import bwapi.{UnitType, UpgradeType}

class ProtossVsProtoss extends AllParallel {
  
  val _threeGateGoons = List[Buildable] (
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Pylon),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Cybernetics_Core),
    new BuildableUnit(UnitType.Protoss_Assimilator),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUpgrade(UpgradeType.Singularity_Charge),
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Assimilator)
  )
  
  val _lateGame = List[Buildable] (
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Forge),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Weapons, 1),
    new BuildableUnit(UnitType.Protoss_Citadel_of_Adun),
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Templar_Archives),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Weapons, 2),
    new BuildableUpgrade(UpgradeType.Protoss_Ground_Weapons, 3),
    new BuildableUnit(UnitType.Protoss_Nexus)
  )
  
  children.set(List(
    new BuildSupplyContinuously,
    new BuildWorkersContinuously,
    new TrainGatewayUnitsContinuously,
    new ScheduleBuildOrder { buildables.set(_threeGateGoons) },
    new ScheduleBuildOrder { buildables.set(_lateGame) },
    new FollowBuildOrder,
    new DefeatWorkerHarass,
    new ScoutAt(20),
    new IfThenElse {
      predicate.set(new UnitCountAtLeast { quantity.set(6); unitMatcher.set(UnitMatchWarriors) })
      whenFalse.set(new DefendChoke)
      whenTrue.set(new Attack)
    },
    new GatherGas,
    new GatherMinerals))
}
