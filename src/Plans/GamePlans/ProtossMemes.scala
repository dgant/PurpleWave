package Plans.GamePlans

import Plans.Army.{Attack, Defend, DefendChoke}
import Plans.Compound.{IfThenElse, Parallel}
import Plans.Information.ScoutAt
import Plans.Macro.Automatic._
import Plans.Macro.Build.{FollowBuildOrder, ScheduleBuildOrder}
import Plans.Macro.UnitCount.UnitCountAtLeast
import Strategies.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import bwapi.UnitType

class ProtossMemes extends Parallel {
  children.set(List(
    new BuildPylonsContinuously,
    new BuildWorkersContinuously,
    new TrainContinuously(UnitType.Protoss_Carrier),
    new TrainContinuously(UnitType.Protoss_Arbiter),
    new TrainContinuously(UnitType.Protoss_Scout),
    new TrainContinuously(UnitType.Protoss_Reaver),
    new TrainGatewayUnitsContinuously,
    new ScheduleBuildOrder { buildables.set(MemeBuild.initialBuild) },
    new ScheduleBuildOrder { buildables.set(MemeBuild.laterBuild) },
    new FollowBuildOrder,
    //new DefeatWorkerHarass,
    new ScoutAt(20),
    new Attack { units.get.unitMatcher.set(new UnitMatchType(UnitType.Protoss_Scout)) },
    new Attack { units.get.unitMatcher.set(new UnitMatchType(UnitType.Protoss_Arbiter)) },
    new Attack { units.get.unitMatcher.set(new UnitMatchType(UnitType.Protoss_Dark_Templar)) },
    new IfThenElse {
      predicate.set(new UnitCountAtLeast { quantity.set(25); unitMatcher.set(UnitMatchWarriors) })
      whenTrue.set(new Attack)
    },
    new GatherGas,
    new GatherMinerals,
    new Defend,
    new IfThenElse {
      predicate.set(new UnitCountAtLeast { quantity.set(2); unitMatcher.set(new UnitMatchType(UnitType.Protoss_Nexus)) })
      whenTrue.set(new DefendChoke { units.get.unitMatcher.set(UnitMatchWarriors) })
    }
  ))
}
