package Planning.Plans.GamePlans

import Macro.BuildRequests._
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{IfThenElse, Or, Parallel}
import Planning.Plans.Information.ScoutAt
import Planning.Plans.Macro.Automatic.{BuildEnoughPylons, TrainContinuously, TrainGatewayUnitsContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.BuildOrders.ScheduleBuildOrder
import Planning.Plans.Macro.UnitCount.{UnitsAtLeast, UnitsExactly}
import ProxyBwapi.Races.Protoss

class ProtossVsProtoss extends Parallel {
  
  description.set("Protoss vs Protoss")
  
  private val _nineNineTwoGate = Vector[BuildRequest] (
    RequestUnitAtLeast(1, Protoss.Nexus),
    RequestUnitAtLeast(8, Protoss.Probe),
    RequestUnitAtLeast(1, Protoss.Pylon),
    RequestUnitAtLeast(9, Protoss.Probe),
    RequestUnitAtLeast(2, Protoss.Gateway),
    RequestUnitAtLeast(11, Protoss.Probe)
  )
  
  private val _earlyZealots = Vector[BuildRequest] (
    RequestUnitAtLeast(1, Protoss.Zealot),
    RequestUnitAtLeast(2, Protoss.Pylon),
    RequestUnitAtLeast(2, Protoss.Zealot)
  )
  
  private val _dragoonTech = Vector[BuildRequest] (
    RequestUnitAtLeast(1, Protoss.Assimilator),
    RequestUnitAtLeast(1, Protoss.CyberneticsCore),
    RequestUpgrade(Protoss.DragoonRange)
  )
  
  private val _firstExpansion = Vector[BuildRequest] (
    RequestUnitAtLeast(2, Protoss.Nexus),
    RequestUnitAtLeast(2, Protoss.Assimilator)
  )
  
  private val _reaverTech = Vector[BuildRequest] (
    RequestUnitAtLeast(1, Protoss.RoboticsFacility),
    RequestUnitAtLeast(1, Protoss.RoboticsSupportBay),
    RequestUpgrade(Protoss.ScarabDamage)
  )
  
  private val _lateGame = Vector[BuildRequest] (
    RequestUnitAtLeast(1, Protoss.RoboticsFacility),
    RequestUnitAtLeast(6, Protoss.Gateway),
    RequestUnitAtLeast(1, Protoss.RoboticsSupportBay),
    RequestUnitAtLeast(3, Protoss.Nexus),
    RequestUnitAtLeast(3, Protoss.Assimilator),
    RequestUnitAtLeast(3, Protoss.RoboticsFacility),
    RequestUnitAtLeast(4, Protoss.Nexus),
    RequestUnitAtLeast(4, Protoss.RoboticsFacility),
    RequestUnitAtLeast(4, Protoss.Assimilator),
    RequestUnitAtLeast(8, Protoss.Gateway),
    RequestUnitAtLeast(5, Protoss.Nexus),
    RequestUnitAtLeast(5, Protoss.Assimilator),
    RequestUnitAtLeast(12, Protoss.Gateway)
  )
  
  children.set(Vector(
    new ScheduleBuildOrder(_nineNineTwoGate),
    new IfThenElse(
      new Or(
        new UnitsExactly(0, UnitMatchType(Protoss.CyberneticsCore)),
        new UnitsExactly(0, UnitMatchType(Protoss.Assimilator))
      ),
      new ScheduleBuildOrder(_earlyZealots)
    ),
    new IfThenElse(
      new UnitsAtLeast(6, UnitMatchType(Protoss.Dragoon)),
      new ScheduleBuildOrder(_firstExpansion)
    ),
    new BuildEnoughPylons,
    new TrainProbesContinuously,
    new TrainContinuously(Protoss.Reaver),
    new TrainGatewayUnitsContinuously,
    //Third gateway in response to pressure
    new ScheduleBuildOrder(_dragoonTech),
    //Add response to DTs
    new ScheduleBuildOrder(_reaverTech),
    new IfThenElse(
      //Replace with building stuff per base
      new UnitsAtLeast(2, UnitMatchType(Protoss.Nexus)),
      new ScheduleBuildOrder(_lateGame)
    ),
    new ScheduleBuildOrder(_firstExpansion),
    //Expand with spare minerals
    new ScoutAt(9),
    new Attack
  ))
}
