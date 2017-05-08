package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests._
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{IfThenElse, Parallel}
import Planning.Plans.Information.{FindExpansions, ScoutAt}
import Planning.Plans.Macro.Automatic.{BuildEnoughPylons, TrainContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.BuildOrders.ScheduleBuildOrder
import Planning.Plans.Macro.UnitCount.{SupplyAtLeast, UnitsAtLeast}
import ProxyBwapi.Races.Protoss

class ProtossVsTerran extends Parallel {
  
  description.set("Protoss vs Terran")
  
  val _lateGame = Vector[BuildRequest] (
    new RequestUnitAtLeast(2,   Protoss.Assimilator),
    new RequestUnitAtLeast(7,   Protoss.Gateway),
    
    new RequestUnitAtLeast(3,   Protoss.Nexus),
    new RequestUnitAtLeast(3,   Protoss.Assimilator),
    new RequestUnitAtLeast(8,   Protoss.Gateway),
    
    new RequestUnitAtLeast(4,   Protoss.Nexus),
    new RequestUnitAtLeast(10,  Protoss.Gateway),
    new RequestUnitAtLeast(4,   Protoss.Assimilator),
    new RequestUnitAtLeast(2,   Protoss.Forge),
    
    new RequestUnitAtLeast(5,   Protoss.Nexus),
    new RequestUnitAtLeast(5,   Protoss.Assimilator),
    new RequestUpgrade(         Protoss.GroundDamage,   1),
    new RequestUpgrade(         Protoss.GroundArmor,    1),
    new RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
    
    new RequestUnitAtLeast(6,   Protoss.Nexus),
    new RequestUnitAtLeast(1,   Protoss.TemplarArchives),
    new RequestUnitAtLeast(15,  Protoss.Gateway),
    new RequestUpgrade(         Protoss.GroundDamage,   2),
    new RequestUpgrade(         Protoss.GroundArmor,    2),
    
    new RequestUnitAtLeast(7,   Protoss.Nexus),
    new RequestUnitAtLeast(7,   Protoss.Assimilator),
    new RequestUpgrade(         Protoss.GroundDamage,   3),
    new RequestUpgrade(         Protoss.GroundArmor,    3),
    
    new RequestUnitAtLeast(8,   Protoss.Nexus),
    new RequestUnitAtLeast(8,   Protoss.Assimilator)
  )
  
  children.set(Vector(
    new ScheduleBuildOrder(ProtossBuilds.OpeningTwoGate1015),
    new BuildEnoughPylons,
    new TrainProbesContinuously,
    new IfThenElse(
      new UnitsAtLeast(6, UnitMatchType(Protoss.Dragoon)),
      new ScheduleBuildOrder(ProtossBuilds.TakeNatural)
    ),
    new TrainContinuously(Protoss.Dragoon),
    new ScheduleBuildOrder(ProtossBuilds.TakeNatural),
    new ScheduleBuildOrder(_lateGame),
    new IfThenElse(
      new SupplyAtLeast(100),
      new FindExpansions
    ),
    new ScoutAt(10),
    new Attack
  ))
}
