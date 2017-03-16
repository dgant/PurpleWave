package Planning.Plans.GamePlans

import Macro.BuildRequests.{BuildRequest, RequestUnitAnother, RequestUnitAnotherOne, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.{Attack, DefendChoke}
import Planning.Plans.Compound.{IfThenElse, Parallel}
import Planning.Plans.Information.ScoutAt
import Planning.Plans.Macro.Build.ScheduleBuildOrder
import Planning.Plans.Macro.UnitCount.UnitCountAtLeast
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass._
import ProxyBwapi.Upgrades.Upgrades
import bwapi.UpgradeType

class ProtossVsProtoss extends Parallel {
  
  description.set("Protoss vs Protoss")
  
  //http://wiki.teamliquid.net/starcraft/4_Gate_Goon_(vs._Protoss)
  val _fourGateGoons = List[BuildRequest] (
    new RequestUnitAnotherOne(Protoss.Nexus),
    new RequestUnitAnother(8, Protoss.Probe),
    new RequestUnitAnotherOne(Protoss.Pylon), //8
    new RequestUnitAnother(2, Protoss.Probe),
    new RequestUnitAnotherOne(Protoss.Gateway), //10
    new RequestUnitAnother(2, Protoss.Probe),
    new RequestUnitAnotherOne(Protoss.Pylon), //12
    new RequestUnitAnotherOne(Protoss.Probe),
    new RequestUnitAnotherOne(Protoss.Zealot), //13
    new RequestUnitAnotherOne(Protoss.Probe),
    new RequestUnitAnotherOne(Protoss.Assimilator), //16
    new RequestUnitAnotherOne(Protoss.Probe),
    new RequestUnitAnotherOne(Protoss.CyberneticsCore), //17
    new RequestUnitAnotherOne(Protoss.Probe),
    new RequestUnitAnotherOne(Protoss.Zealot), //18
    new RequestUnitAnotherOne(Protoss.Probe),
    new RequestUnitAnotherOne(Protoss.Probe),
    new RequestUnitAnotherOne(Protoss.Pylon), //22
    new RequestUnitAnotherOne(Protoss.Probe),
    new RequestUnitAnotherOne(Protoss.Dragoon), //23
    new RequestUnitAnotherOne(Protoss.Probe),
    new RequestUpgrade(Upgrades.get(UpgradeType.Singularity_Charge)), //26
    new RequestUnitAnotherOne(Protoss.Probe),
    new RequestUnitAnotherOne(Protoss.Dragoon), //27
    new RequestUnitAnotherOne(Protoss.Probe),
    new RequestUnitAnotherOne(Protoss.Probe),
    new RequestUnitAnotherOne(Protoss.Gateway), //31
    new RequestUnitAnotherOne(Protoss.Gateway), //31
    new RequestUnitAnotherOne(Protoss.Gateway), //31
    new RequestUnitAnotherOne(Protoss.Dragoon), //31
    new RequestUnitAnotherOne(Protoss.Pylon), //33
    new RequestUnitAnotherOne(Protoss.Dragoon), //33
    new RequestUnitAnotherOne(Protoss.Dragoon),
    new RequestUnitAnotherOne(Protoss.Dragoon),
    new RequestUnitAnotherOne(Protoss.Dragoon),
    new RequestUnitAnotherOne(Protoss.Pylon), //33
    new RequestUnitAnotherOne(Protoss.Dragoon), //33
    new RequestUnitAnotherOne(Protoss.Dragoon),
    new RequestUnitAnotherOne(Protoss.Dragoon),
    new RequestUnitAnotherOne(Protoss.Dragoon),
    new RequestUnitAnotherOne(Protoss.Nexus)
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
