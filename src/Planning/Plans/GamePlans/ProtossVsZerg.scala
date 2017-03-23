package Planning.Plans.GamePlans

import Macro.BuildRequests.{BuildRequest, RequestUnitAnotherOne, RequestUnitAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army.{Attack, DefendChoke, Hunt}
import Planning.Plans.Compound.{IfThenElse, Parallel}
import Planning.Plans.Information.ScoutAt
import Planning.Plans.Macro.Automatic.{BuildPylonsContinuously, TrainContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.BuildOrders.ScheduleBuildOrder
import Planning.Plans.Macro.UnitCount.UnitCountAtLeast
import ProxyBwapi.Races.Protoss

class ProtossVsZerg extends Parallel {
  
  description.set("Protoss vs Zerg")
  
  // http://wiki.teamliquid.net/starcraft/Protoss_vs._Zerg_Guide#Branch_II:_Two_Gateways
  val _oneBaseTwoGate = List[BuildRequest] (
    new RequestUnitAnotherOne(Protoss.Nexus),
    new RequestUnitAtLeast(8,   Protoss.Probe),
    new RequestUnitAnotherOne(  Protoss.Pylon),
    new RequestUnitAtLeast(10,  Protoss.Probe),
    new RequestUnitAnotherOne(  Protoss.Gateway),
    new RequestUnitAtLeast(12,  Protoss.Probe),
    new RequestUnitAnotherOne(  Protoss.Gateway),
    new RequestUnitAtLeast(13,  Protoss.Probe),
    new RequestUnitAtLeast(1,   Protoss.Zealot),
    new RequestUnitAtLeast(14,  Protoss.Probe),
    new RequestUnitAtLeast(2,   Protoss.Pylon),
    new RequestUnitAtLeast(15,  Protoss.Probe),
    new RequestUnitAtLeast(3,   Protoss.Zealot)
  )
  
  val _twoBase = List[BuildRequest] (
    new RequestUnitAtLeast(2,   Protoss.Nexus),
    new RequestUnitAtLeast(1,   Protoss.Assimilator),
    new RequestUnitAtLeast(1,   Protoss.CyberneticsCore),
    new RequestUnitAtLeast(2,   Protoss.Assimilator),
    new RequestUnitAtLeast(3,   Protoss.Gateway),
    new RequestUnitAtLeast(1,   Protoss.Stargate),
    new RequestUnitAtLeast(1,   Protoss.RoboticsFacility),
    new RequestUnitAtLeast(1,   Protoss.RoboticsSupportBay),
    
    new RequestUnitAtLeast(3,   Protoss.Nexus),
    new RequestUnitAtLeast(3,   Protoss.Assimilator),
    new RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
    new RequestUpgrade(         Protoss.DragoonRange),
    new RequestUnitAtLeast(6,   Protoss.Gateway),
    new RequestUnitAtLeast(1,   Protoss.TemplarArchives),
    
    new RequestUnitAtLeast(4,   Protoss.Nexus),
    new RequestUnitAtLeast(4,   Protoss.Assimilator),
    new RequestUnitAtLeast(3,   Protoss.DarkTemplar),
    new RequestUnitAtLeast(8,   Protoss.Gateway),
    new RequestUnitAtLeast(1,   Protoss.Forge),
    new RequestUpgrade(         Protoss.ZealotLegs),
    new RequestUpgrade(         Protoss.GroundWeapons, 1),
    
    new RequestUnitAtLeast(5,   Protoss.Nexus),
    new RequestUnitAtLeast(8,   Protoss.Gateway),
    new RequestUnitAtLeast(5,   Protoss.Assimilator),
    new RequestUnitAtLeast(2,   Protoss.RoboticsFacility),
    new RequestUnitAtLeast(1,   Protoss.TemplarArchives),
    new RequestUnitAtLeast(3,   Protoss.DarkTemplar),
    new RequestUpgrade(         Protoss.GroundWeapons, 2),
    
    new RequestUnitAtLeast(6,   Protoss.Nexus),
    new RequestUnitAtLeast(6,   Protoss.Assimilator),
    new RequestUnitAtLeast(12,  Protoss.Gateway),
    new RequestUpgrade(         Protoss.GroundWeapons, 3)
  )
  
  val _enoughZealots = List[BuildRequest] (new RequestUnitAtLeast(10, Protoss.Zealot))
  
  children.set(List(
    new ScheduleBuildOrder { buildables.set(_oneBaseTwoGate) },
    new BuildPylonsContinuously,
    new TrainProbesContinuously,
    new TrainContinuously(Protoss.Reaver),
    new TrainContinuously(Protoss.Corsair),
    new TrainContinuously(Protoss.Dragoon),
    new ScheduleBuildOrder { buildables.set(_enoughZealots) },
    new ScheduleBuildOrder { buildables.set(_twoBase) },
    new ScoutAt(20),
    new IfThenElse {
      predicate.set(new UnitCountAtLeast { quantity.set(8); unitMatcher.set(UnitMatchWarriors) })
      whenFalse.set(new DefendChoke)
      whenTrue.set(new Attack)
    },
    new Hunt { hunters.get.unitMatcher.set(new UnitMatchType(Protoss.Corsair)) }
  ))
}
