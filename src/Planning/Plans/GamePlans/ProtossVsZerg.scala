package Planning.Plans.GamePlans

import Macro.BuildRequests.{BuildRequest, RequestUnitAnotherOne, RequestUnitAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army.{Attack, Defend}
import Planning.Plans.Compound.{And, IfThenElse, Parallel}
import Planning.Plans.Information.ScoutAt
import Planning.Plans.Macro.Automatic.{BuildEnoughPylons, TrainContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.BuildOrders.ScheduleBuildOrder
import Planning.Plans.Macro.UnitCount.UnitsAtLeast
import ProxyBwapi.Races.Protoss

class ProtossVsZerg extends Parallel {
  
  description.set("Protoss vs Zerg")
  
  val _nineNineGateways = Vector[BuildRequest] (
    new RequestUnitAnotherOne(Protoss.Nexus),
    new RequestUnitAtLeast(9,   Protoss.Probe),
    new RequestUnitAtLeast(1,   Protoss.Pylon),
    new RequestUnitAtLeast(2,   Protoss.Gateway)
  )
  
  val _earlyZealots = Vector[BuildRequest] (
    new RequestUnitAtLeast(1,   Protoss.Zealot),
    new RequestUnitAtLeast(2,   Protoss.Pylon),
    new RequestUnitAtLeast(2,   Protoss.Zealot)
  )
  
  val _firstExpansion = Vector[BuildRequest] (
    new RequestUnitAtLeast(2,   Protoss.Nexus),
  )
  
  val _twoBase = Vector[BuildRequest] (
    new RequestUnitAtLeast(2,   Protoss.Nexus),
    new RequestUnitAtLeast(3,   Protoss.Gateway),
    new RequestUnitAtLeast(1,   Protoss.CyberneticsCore),
    new RequestUnitAtLeast(2,   Protoss.Assimilator),
    new RequestUnitAtLeast(1,   Protoss.Stargate),
    new RequestUnitAtLeast(1,   Protoss.Forge),
    new RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
    new RequestUpgrade(         Protoss.ZealotSpeed),
    new RequestUpgrade(         Protoss.GroundDamage, 1),
    new RequestUnitAtLeast(2,   Protoss.PhotonCannon),
    
    new RequestUnitAtLeast(3,   Protoss.Nexus),
    new RequestUnitAtLeast(1,   Protoss.RoboticsFacility),
    new RequestUnitAtLeast(4,   Protoss.PhotonCannon),
    new RequestUnitAtLeast(3,   Protoss.Assimilator),
    new RequestUpgrade(         Protoss.DragoonRange),
    new RequestUnitAtLeast(1,   Protoss.RoboticsSupportBay),
    new RequestUnitAtLeast(5,   Protoss.Gateway),
    new RequestUnitAtLeast(4,   Protoss.PhotonCannon),
    
    new RequestUnitAtLeast(4,   Protoss.Nexus),
    new RequestUnitAtLeast(2,   Protoss.RoboticsFacility),
    new RequestUnitAtLeast(4,   Protoss.Assimilator),
    new RequestUnitAtLeast(1,   Protoss.TemplarArchives),
    new RequestUnitAtLeast(2,   Protoss.Forge),
    new RequestUnitAtLeast(7,   Protoss.PhotonCannon),
    new RequestUpgrade(         Protoss.GroundDamage, 2),
    new RequestUpgrade(         Protoss.GroundArmor, 2),
    new RequestUnitAtLeast(3,   Protoss.DarkTemplar),
    new RequestUnitAtLeast(8,   Protoss.Gateway),
    
    new RequestUnitAtLeast(5,   Protoss.Nexus),
    new RequestUnitAtLeast(10,  Protoss.Gateway),
    new RequestUnitAtLeast(5,   Protoss.Assimilator),
    new RequestUpgrade(         Protoss.GroundDamage, 3),
    new RequestUpgrade(         Protoss.GroundArmor, 3),
    
    new RequestUnitAtLeast(6,   Protoss.Nexus),
    new RequestUnitAtLeast(12,  Protoss.Gateway),
    new RequestUnitAtLeast(6,   Protoss.Assimilator),
    
    new RequestUnitAtLeast(7,   Protoss.Nexus),
    new RequestUnitAtLeast(7,   Protoss.Assimilator),
    
    new RequestUnitAtLeast(8,   Protoss.Nexus),
    new RequestUnitAtLeast(8,   Protoss.Assimilator)
  )
  
  children.set(Vector(
    new ScheduleBuildOrder(_nineNineGateways),
    new BuildEnoughPylons,
    new TrainProbesContinuously,
    new TrainContinuously(Protoss.Reaver),
    new TrainContinuously(Protoss.Corsair),
    new IfThenElse(
      new And(
        new UnitsAtLeast(1, new UnitMatchType(Protoss.CyberneticsCore)),
        new UnitsAtLeast(1, new UnitMatchType(Protoss.Assimilator))
      ),
      new IfThenElse (
        new UnitsAtLeast(8, new UnitMatchType(Protoss.Zealot)),
        new TrainContinuously(Protoss.Dragoon),
        new TrainContinuously(Protoss.Zealot)
      ),
      new Parallel(
        new ScheduleBuildOrder(_earlyZealots),
        new TrainContinuously(Protoss.Zealot)
      )
    ),
    new ScheduleBuildOrder(_twoBase),
    new ScoutAt(10),
    new Attack{ attackers.get.unitMatcher.set(new UnitMatchType(Protoss.Corsair)) },
    new IfThenElse(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new Attack,
      new Defend
    )
  ))
}
