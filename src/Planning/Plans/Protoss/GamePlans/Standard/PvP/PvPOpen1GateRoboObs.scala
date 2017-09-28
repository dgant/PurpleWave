package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Compound.{Or, Trigger}
import Planning.Plans.GamePlans.TemplateMode
import Planning.Plans.Information.Employing
import Planning.Plans.Information.Reactive.EnemyBasesAtLeast
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvP1GateRoboObs

class PvPOpen1GateRoboObs extends TemplateMode {
  
  override val activationCriteria: Plan = new Employing(PvP1GateRoboObs)
  
  override val completionCriteria: Plan = new Or(
    new EnemyBasesAtLeast(2),
    new UnitsAtLeast(1, Protoss.Observer))
  
  override val buildOrder = Vector(
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),             // 8
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),           // 10
    RequestAtLeast(11,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),       // 11
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(2,   Protoss.Pylon),             // 16 = 14 + Z
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Dragoon),           // 18 = 16 + Z
    RequestUpgrade(Protoss.DragoonRange),           // 20 = 16 + Z + D
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),             // 21 = 17 + Z + D
    RequestAtLeast(18,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Dragoon),           // 22 = 18 + Z + D
    RequestAtLeast(20,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.RoboticsFacility),  // 26 = 20 + Z + DD
    RequestAtLeast(21,  Protoss.Probe),             // Now probe cut
    RequestAtLeast(3,   Protoss.Dragoon),           // TL is unclear whether this should be a Dragoon or two more Probes
    RequestAtLeast(3,   Protoss.Gateway),           // 29 = 21 + Z + DDD
    RequestAtLeast(4,   Protoss.Dragoon),           // 29 = 21 + Z + DDD
    RequestAtLeast(22,  Protoss.Probe),
    RequestAtLeast(4,   Protoss.Pylon),
    RequestAtLeast(23,  Protoss.Probe),
    RequestAtLeast(5,   Protoss.Dragoon),           // 31 = 23 + Z + DDDD
    RequestAtLeast(1,   Protoss.Observatory))       // 33 = 23 + Z + DDDD
  
  override val buildPlans: Vector[Plan] = Vector(
    new TrainContinuously(Protoss.Observer, 1),
    new TrainContinuously(Protoss.Dragoon),
    new RequireMiningBases(2),
    new Build(RequestAtLeast(4, Protoss.Gateway)))
  
  override val defaultScoutPlan: Plan = new Trigger(new UnitsAtLeast(1, Protoss.CyberneticsCore), new Scout)
}
