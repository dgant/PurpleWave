package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{RequestUnitAtLeast, _}
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{IfThenElse, Or, Parallel}
import Planning.Plans.Information.{FindExpansions, ScoutAt}
import Planning.Plans.Macro.Automatic.{BuildEnoughPylons, TrainContinuously, TrainGatewayUnitsContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.BuildOrders.ScheduleBuildOrder
import Planning.Plans.Macro.UnitCount.{SupplyAtLeast, UnitsAtLeast, UnitsExactly}
import ProxyBwapi.Races.Protoss

class ProtossVsProtoss extends Parallel {
  
  description.set("Protoss vs Protoss")
  
  private val _thirdGateway = Vector[BuildRequest] (
    RequestUnitAtLeast(3, Protoss.Gateway)
  )
  
  private val _firstExpansion = Vector[BuildRequest] (
    RequestUnitAtLeast(2, Protoss.Nexus)
  )
  
  private val _lateGame = Vector[BuildRequest] (
    RequestUnitAtLeast(2,   Protoss.Assimilator),
    RequestUnitAtLeast(1,   Protoss.RoboticsFacility),
    RequestUnitAtLeast(6,   Protoss.Gateway),
    RequestUnitAtLeast(1,   Protoss.RoboticsSupportBay),
    RequestUnitAtLeast(3,   Protoss.Nexus),
    RequestUnitAtLeast(3,   Protoss.Assimilator),
    RequestUnitAtLeast(8,   Protoss.Gateway),
    RequestUnitAtLeast(4,   Protoss.Nexus),
    RequestUnitAtLeast(4,   Protoss.Assimilator),
    RequestUnitAtLeast(12,  Protoss.Gateway),
    RequestUnitAtLeast(5,   Protoss.Nexus),
    RequestUnitAtLeast(5,   Protoss.Assimilator),
    RequestUnitAtLeast(6,   Protoss.Nexus),
    RequestUnitAtLeast(6,   Protoss.Assimilator),
    RequestUnitAtLeast(6,   Protoss.Nexus),
    RequestUnitAtLeast(6,   Protoss.Assimilator)
  )
  
  children.set(Vector(
    new ScheduleBuildOrder(ProtossBuilds.TwoGate99),
    new IfThenElse(
      new Or(
        new UnitsExactly(0, UnitMatchType(Protoss.CyberneticsCore)),
        new UnitsExactly(0, UnitMatchType(Protoss.Assimilator))
      ),
      new ScheduleBuildOrder(ProtossBuilds.TwoGate99Zealots)
    ),
    new IfThenElse(
      new UnitsAtLeast(6, UnitMatchType(Protoss.Dragoon)),
      new ScheduleBuildOrder(ProtossBuilds.TakeNatural)
    ),
    new BuildEnoughPylons,
    new TrainProbesContinuously,
    new TrainContinuously(Protoss.Reaver, 5),
    new TrainGatewayUnitsContinuously,
    new ScheduleBuildOrder(ProtossBuilds.TechDragoons),
    //Make this reactive
    new ScheduleBuildOrder(_thirdGateway),
    //Add response to DTs
    new ScheduleBuildOrder(ProtossBuilds.TechReavers),
    new ScheduleBuildOrder(ProtossBuilds.TakeNatural),
    //Replace lateGame with building stuff per base
    new ScheduleBuildOrder(_lateGame),
    new IfThenElse(
      new SupplyAtLeast(100),
      new FindExpansions
    ),
    new ScoutAt(9),
    new Attack
  ))
}
