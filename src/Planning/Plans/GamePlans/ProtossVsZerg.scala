package Planning.Plans.GamePlans

import Planning.Plans.Army.{Attack, DefendChoke}
import Planning.Plans.Compound.{IfThenElse, Parallel}
import Planning.Plans.Information.ScoutAt
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.ScheduleBuildOrder
import Planning.Plans.Macro.UnitCount.UnitCountAtLeast
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Macro.BuildRequests.{BuildRequest, RequestUnitAnotherOne, RequestUpgrade}
import bwapi.{UnitType, UpgradeType}

class ProtossVsZerg extends Parallel {
  
  description.set("Protoss vs Zerg")
  
  // http://wiki.teamliquid.net/starcraft/One_Base_Speedzeal_(vs._Zerg)
  val _oneBaseSpeedlot = List[BuildRequest] (
    new RequestUnitAnotherOne(UnitType.Protoss_Nexus),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Pylon), //8
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Gateway), //10
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Assimilator), //12
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Zealot), //13
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Pylon), //16
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Zealot), //17
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Cybernetics_Core), //20
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Zealot), //21
    new RequestUnitAnotherOne(UnitType.Protoss_Pylon), //23
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Dragoon), //25
    new RequestUnitAnotherOne(UnitType.Protoss_Citadel_of_Adun), //27
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Gateway), //29
    new RequestUnitAnotherOne(UnitType.Protoss_Zealot), //29
    new RequestUnitAnotherOne(UnitType.Protoss_Pylon), //31
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUpgrade(UpgradeType.Leg_Enhancements), //32
    new RequestUnitAnotherOne(UnitType.Protoss_Zealot), //32
    new RequestUnitAnotherOne(UnitType.Protoss_Zealot), //34
    new RequestUnitAnotherOne(UnitType.Protoss_Probe), //36
    new RequestUnitAnotherOne(UnitType.Protoss_Pylon), //37
    new RequestUnitAnotherOne(UnitType.Protoss_Zealot), //37
    new RequestUnitAnotherOne(UnitType.Protoss_Zealot), //39
    new RequestUnitAnotherOne(UnitType.Protoss_Nexus),
    new RequestUnitAnotherOne(UnitType.Protoss_Gateway),
    new RequestUnitAnotherOne(UnitType.Protoss_Gateway)
  )
  
  children.set(List(
    new ScheduleBuildOrder { buildables.set(_oneBaseSpeedlot) },
    new BuildPylonsContinuously,
    new BuildWorkersContinuously,
    new TrainContinuously(UnitType.Protoss_Scout),
    new TrainContinuously(UnitType.Protoss_Zealot),
    new ScheduleBuildOrder { buildables.set(MassScoutLateGame.build) },
    new ScoutAt(20),
    new IfThenElse {
      predicate.set(new UnitCountAtLeast { quantity.set(9); unitMatcher.set(UnitMatchWarriors) })
      whenFalse.set(new DefendChoke)
      whenTrue.set(new Attack)
    }
  ))
}
