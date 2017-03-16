package Planning.Plans.GamePlans

import Macro.BuildRequests.{BuildRequest, RequestUnitAnotherOne, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.{Attack, DefendChoke}
import Planning.Plans.Compound.{IfThenElse, Parallel}
import Planning.Plans.Information.ScoutAt
import Planning.Plans.Macro.Build.ScheduleBuildOrder
import Planning.Plans.Macro.UnitCount.UnitCountAtLeast
import ProxyBwapi.UnitClass._
import ProxyBwapi.Upgrades.Upgrades
import bwapi.UpgradeType

class ProtossVsProtoss extends Parallel {
  
  description.set("Protoss vs Protoss")
  
  //http://wiki.teamliquid.net/starcraft/4_Gate_Goon_(vs._Protoss)
  val _fourGateGoons = List[BuildRequest] (
    new RequestUnitAnotherOne(Nexus),
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Pylon), //8
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Gateway), //10
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Pylon), //12
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Zealot), //13
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Assimilator), //16
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(CyberneticsCore), //17
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Zealot), //18
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Pylon), //22
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Dragoon), //23
    new RequestUnitAnotherOne(Probe),
    new RequestUpgrade(Upgrades.get(UpgradeType.Singularity_Charge)), //26
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Dragoon), //27
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Probe),
    new RequestUnitAnotherOne(Gateway), //31
    new RequestUnitAnotherOne(Gateway), //31
    new RequestUnitAnotherOne(Gateway), //31
    new RequestUnitAnotherOne(Dragoon), //31
    new RequestUnitAnotherOne(Pylon), //33
    new RequestUnitAnotherOne(Dragoon), //33
    new RequestUnitAnotherOne(Dragoon),
    new RequestUnitAnotherOne(Dragoon),
    new RequestUnitAnotherOne(Dragoon),
    new RequestUnitAnotherOne(Pylon), //33
    new RequestUnitAnotherOne(Dragoon), //33
    new RequestUnitAnotherOne(Dragoon),
    new RequestUnitAnotherOne(Dragoon),
    new RequestUnitAnotherOne(Dragoon),
    new RequestUnitAnotherOne(Nexus)
  )
  
  children.set(List(
    new ScheduleBuildOrder { buildables.set(_fourGateGoons) },
    //new BuildPylonsContinuously,
    //new TrainProbesContinuously,
    //new TrainContinuously(Scout),
    //new TrainContinuously(Zealot),
    new ScoutAt(20),
    new IfThenElse {
      predicate.set(new UnitCountAtLeast { quantity.set(6); unitMatcher.set(UnitMatchWarriors) })
      whenFalse.set(new DefendChoke)
      whenTrue.set(new Attack)
    }
  ))
}
