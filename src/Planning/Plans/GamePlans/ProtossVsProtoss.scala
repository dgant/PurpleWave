package Planning.Plans.GamePlans

import Macro.BuildRequests._
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.{Attack, Defend}
import Planning.Plans.Compound.{IfThenElse, Parallel}
import Planning.Plans.Information.ScoutAt
import Planning.Plans.Macro.Automatic.{BuildEnoughPylons, TrainContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.BuildOrders.ScheduleBuildOrder
import Planning.Plans.Macro.UnitCount.UnitCountAtLeast
import ProxyBwapi.Races.Protoss

class ProtossVsProtoss extends Parallel {
  
  description.set("Protoss vs Protoss")
  
  // http://wiki.teamliquid.net/starcraft/4_Gate_Goon_(vs._Protoss)
  
  
  val _fourGateGoonsSimplifiedStart = List[BuildRequest] (
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
    new RequestUnitAnotherOne(Protoss.Probe)
  )
  
  val _fourGateGoonsSimplified = List[BuildRequest] (
    new RequestUpgrade(Protoss.DragoonRange),
    new RequestUnitAtLeast(4, Protoss.Gateway),
    new RequestUnitAnotherOne(Protoss.Nexus)
  )
    
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
    new RequestUpgrade(Protoss.DragoonRange), //26
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
  
  val _twoBaseBuild = List[BuildRequest] (
    new RequestUnitAtLeast(2, Protoss.Assimilator),
    new RequestUnitAtLeast(1, Protoss.RoboticsFacility),
    new RequestUnitAtLeast(6, Protoss.Gateway),
    new RequestUnitAtLeast(1, Protoss.RoboticsSupportBay),
    new RequestUnitAtLeast(3, Protoss.Nexus),
    new RequestUnitAtLeast(3, Protoss.Assimilator),
    new RequestUpgrade(Protoss.ScarabDamage),
    new RequestUnitAtLeast(3, Protoss.RoboticsFacility),
    new RequestUnitAtLeast(4, Protoss.Nexus),
    new RequestUnitAtLeast(4, Protoss.RoboticsFacility),
    new RequestUnitAtLeast(4, Protoss.Assimilator),
    new RequestUnitAtLeast(8, Protoss.Gateway),
    new RequestUnitAtLeast(5, Protoss.Nexus),
    new RequestUnitAtLeast(5, Protoss.Assimilator),
    new RequestUnitAtLeast(12, Protoss.Gateway)
  )
  
  children.set(List(
    new ScheduleBuildOrder { buildables.set(_fourGateGoonsSimplifiedStart) },
    new BuildEnoughPylons,
    new TrainProbesContinuously,
    new TrainContinuously(Protoss.Reaver),
    new TrainContinuously(Protoss.Dragoon),
    new TrainContinuously(Protoss.Zealot),
    new ScheduleBuildOrder { buildables.set(_fourGateGoonsSimplified) },
    new ScheduleBuildOrder { buildables.set(_twoBaseBuild) },
    new ScoutAt(10),
    new IfThenElse {
      predicate.set(new UnitCountAtLeast { quantity.set(6); unitMatcher.set(UnitMatchWarriors) })
      whenFalse.set(new Defend)
      whenTrue.set(new Attack)
    }
  ))
}
