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
  
  // http://wiki.teamliquid.net/starcraft/4_Gate_Goon_(vs._Protoss)
  
  
  val _nineNineTwoGate = Vector[BuildRequest] (
    new RequestUnitAtLeast(1, Protoss.Nexus),
    new RequestUnitAtLeast(9, Protoss.Probe),
    new RequestUnitAtLeast(1, Protoss.Pylon),
    new RequestUnitAtLeast(2, Protoss.Gateway),
    new RequestUnitAtLeast(11, Protoss.Probe)
  )
  
  val _earlyZealots = Vector[BuildRequest] (
    new RequestUnitAtLeast(1, Protoss.Zealot),
    new RequestUnitAtLeast(2, Protoss.Pylon),
    new RequestUnitAtLeast(2, Protoss.Zealot)
  )
  
  val _dragoonTech = Vector[BuildRequest] (
    new RequestUnitAtLeast(1, Protoss.Assimilator),
    new RequestUnitAtLeast(1, Protoss.CyberneticsCore),
    new RequestUpgrade(Protoss.DragoonRange)
  )
  
  val _firstExpansion = Vector[BuildRequest] (
    new RequestUnitAtLeast(2, Protoss.Nexus),
    new RequestUnitAtLeast(2, Protoss.Assimilator)
  )
  
  val _reaverTech = Vector[BuildRequest] (
    new RequestUnitAtLeast(1, Protoss.RoboticsFacility),
    new RequestUnitAtLeast(1, Protoss.RoboticsSupportBay),
    new RequestUpgrade(Protoss.ScarabDamage)
  )
  
  val _lateGame = Vector[BuildRequest] (
    new RequestUnitAtLeast(1, Protoss.RoboticsFacility),
    new RequestUnitAtLeast(6, Protoss.Gateway),
    new RequestUnitAtLeast(1, Protoss.RoboticsSupportBay),
    new RequestUnitAtLeast(3, Protoss.Nexus),
    new RequestUnitAtLeast(3, Protoss.Assimilator),
    new RequestUnitAtLeast(3, Protoss.RoboticsFacility),
    new RequestUnitAtLeast(4, Protoss.Nexus),
    new RequestUnitAtLeast(4, Protoss.RoboticsFacility),
    new RequestUnitAtLeast(4, Protoss.Assimilator),
    new RequestUnitAtLeast(8, Protoss.Gateway),
    new RequestUnitAtLeast(5, Protoss.Nexus),
    new RequestUnitAtLeast(5, Protoss.Assimilator),
    new RequestUnitAtLeast(12, Protoss.Gateway)
  )
  
  children.set(Vector(
    new ScheduleBuildOrder(_nineNineTwoGate),
    new IfThenElse(
      new Or(
        new UnitsExactly(0, new UnitMatchType(Protoss.CyberneticsCore)),
        new UnitsExactly(0, new UnitMatchType(Protoss.Assimilator))
      ),
      new ScheduleBuildOrder(_earlyZealots)
    ),
    new BuildEnoughPylons,
    new TrainProbesContinuously,
    new TrainContinuously(Protoss.Reaver),
    new TrainGatewayUnitsContinuously,
    new ScheduleBuildOrder(_dragoonTech),
    new IfThenElse(
      new UnitsAtLeast(6, new UnitMatchType(Protoss.Dragoon)),
      new ScheduleBuildOrder(_firstExpansion)
    ),
    new ScheduleBuildOrder(_reaverTech),
    new IfThenElse(
      new UnitsAtLeast(2, new UnitMatchType(Protoss.Nexus)),
      new ScheduleBuildOrder(_lateGame)
    ),
    new ScoutAt(9),
    new Attack
  ))
}
