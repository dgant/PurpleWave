package Planning.Plans.GamePlans

import Macro.BuildRequests._
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.{Attack, DefendChoke}
import Planning.Plans.Compound.{IfThenElse, Parallel}
import Planning.Plans.Information.ScoutAt
import Planning.Plans.Macro.Automatic.{BuildPylonsContinuously, TrainContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.BuildOrders.ScheduleBuildOrder
import Planning.Plans.Macro.UnitCount.UnitCountAtLeast
import ProxyBwapi.Races.Protoss

class ProtossVsTerran extends Parallel {
  
  description.set("Protoss vs Terran")
  
  // http://wiki.teamliquid.net/starcraft/14_Nexus_(vs._Terran)
  val _13Nexus = List[BuildRequest] (
    new RequestUnitAnotherOne(Protoss.Nexus),
    new RequestUnitAtLeast(8,   Protoss.Probe),
    new RequestUnitAtLeast(1,   Protoss.Pylon),
    new RequestUnitAtLeast(13,  Protoss.Probe),
    new RequestUnitAtLeast(2,   Protoss.Nexus),
    new RequestUnitAtLeast(14,  Protoss.Probe),
    new RequestUnitAtLeast(1,   Protoss.Gateway),
    new RequestUnitAtLeast(15,  Protoss.Probe),
    new RequestUnitAtLeast(1,   Protoss.Assimilator),
    new RequestUnitAtLeast(17,  Protoss.Probe),
    new RequestUnitAtLeast(1,   Protoss.CyberneticsCore),
    new RequestUnitAtLeast(2,   Protoss.Gateway),
    new RequestUnitAtLeast(1,   Protoss.Zealot),
    new RequestUnitAtLeast(19,  Protoss.Probe),
    new RequestUnitAtLeast(2,   Protoss.Pylon),
    new RequestUnitAtLeast(2,   Protoss.Dragoon),
    new RequestUpgrade(         Protoss.DragoonRange),
    new RequestUnitAtLeast(21,  Protoss.Probe),
    new RequestUnitAtLeast(3,   Protoss.Pylon),
    new RequestUnitAtLeast(4,   Protoss.Dragoon),
    new RequestUnitAtLeast(23,  Protoss.Probe),
    new RequestUnitAtLeast(3,   Protoss.Pylon),
    new RequestUnitAtLeast(6,   Protoss.Dragoon)
  )
  
  val _lateGame = List[BuildRequest] (
    new RequestUnitAtLeast(3, Protoss.Nexus),
    new RequestUnitAtLeast(8, Protoss.Dragoon),
    new RequestUnitAtLeast(1, Protoss.Stargate),
    new RequestUnitAtLeast(3, Protoss.Gateway),
    new RequestUnitAtLeast(1, Protoss.FleetBeacon),
    new RequestUnitAtLeast(2, Protoss.Stargate),
    new RequestUnitAtLeast(3, Protoss.Assimilator),
    new RequestUnitAtLeast(4, Protoss.Nexus),
    new RequestUnitAtLeast(4, Protoss.Assimilator),
    new RequestUpgrade(Protoss.AirWeapons, 1),
    new RequestUnitAtLeast(4, Protoss.Stargate),
    new RequestUpgrade(Protoss.AirWeapons, 2),
    new RequestUnitAtLeast(5, Protoss.Nexus),
    new RequestUnitAtLeast(5, Protoss.Assimilator),
    new RequestUnitAtLeast(5, Protoss.Stargate),
    new RequestUpgrade(Protoss.AirWeapons, 3),
    new RequestUnitAtLeast(6, Protoss.Nexus),
    new RequestUnitAtLeast(6, Protoss.Assimilator),
    new RequestUpgrade(Protoss.AirArmor, 1),
    new RequestUpgrade(Protoss.AirArmor, 2),
    new RequestUpgrade(Protoss.AirArmor, 3)
  )
  
  children.set(List(
    new ScheduleBuildOrder { buildables.set(_13Nexus) },
    new BuildPylonsContinuously,
    new TrainProbesContinuously,
    new TrainContinuously(Protoss.Carrier),
    new TrainContinuously(Protoss.Zealot),
    new ScheduleBuildOrder { buildables.set(_lateGame) },
    new ScoutAt(20),
    new IfThenElse {
      predicate.set(new UnitCountAtLeast { quantity.set(6); unitMatcher.set(UnitMatchWarriors) })
      whenFalse.set(new DefendChoke)
      whenTrue.set(new Attack)
    }
  ))
}
