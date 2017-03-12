package Plans.GamePlans

import Plans.Army.{Attack, DefendChoke}
import Plans.Compound.{IfThenElse, Parallel}
import Plans.Defense.DefeatWorkerHarass
import Plans.Information.ScoutAt
import Plans.Macro.Automatic._
import Plans.Macro.Build.{FollowBuildOrder, ScheduleBuildOrder}
import Plans.Macro.UnitCount.UnitCountAtLeast
import Strategies.UnitMatchers.UnitMatchWarriors
import Types.Buildable.{Buildable, BuildableUnit, BuildableUpgrade}
import bwapi.{UnitType, UpgradeType}

class ProtossVsZerg extends Parallel {
  
  description.set("Protoss vs Zerg")
  
  // http://wiki.teamliquid.net/starcraft/One_Base_Speedzeal_(vs._Zerg)
  val _oneBaseSpeedlot = List[Buildable] (
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Pylon), //8
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Gateway), //10
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Assimilator), //12
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Zealot), //13
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Pylon), //16
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Zealot), //17
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Cybernetics_Core), //20
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Zealot), //21
    new BuildableUnit(UnitType.Protoss_Pylon), //23
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Dragoon), //25
    new BuildableUnit(UnitType.Protoss_Citadel_of_Adun), //27
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUnit(UnitType.Protoss_Gateway), //29
    new BuildableUnit(UnitType.Protoss_Zealot), //29
    new BuildableUnit(UnitType.Protoss_Pylon), //31
    new BuildableUnit(UnitType.Protoss_Probe),
    new BuildableUpgrade(UpgradeType.Leg_Enhancements), //32
    new BuildableUnit(UnitType.Protoss_Zealot), //32
    new BuildableUnit(UnitType.Protoss_Zealot), //34
    new BuildableUnit(UnitType.Protoss_Probe), //36
    new BuildableUnit(UnitType.Protoss_Pylon), //37
    new BuildableUnit(UnitType.Protoss_Zealot), //37
    new BuildableUnit(UnitType.Protoss_Zealot), //39
    new BuildableUnit(UnitType.Protoss_Nexus),
    new BuildableUnit(UnitType.Protoss_Gateway),
    new BuildableUnit(UnitType.Protoss_Gateway)
  )
  
  children.set(List(
    new ScheduleBuildOrder { buildables.set(_oneBaseSpeedlot) },
    new BuildPylonsContinuously,
    new BuildWorkersContinuously,
    new TrainContinuously(UnitType.Protoss_Scout),
    new TrainContinuously(UnitType.Protoss_Zealot),
    new ScheduleBuildOrder { buildables.set(MassScoutLateGame.build) },
    new FollowBuildOrder,
    new DefeatWorkerHarass,
    new ScoutAt(20),
    new IfThenElse {
      predicate.set(new UnitCountAtLeast { quantity.set(9); unitMatcher.set(UnitMatchWarriors) })
      whenFalse.set(new DefendChoke)
      whenTrue.set(new Attack)
    },
    new GatherGas,
    new GatherMinerals))
}
