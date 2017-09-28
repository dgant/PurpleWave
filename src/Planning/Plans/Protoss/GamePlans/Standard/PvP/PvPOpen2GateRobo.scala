package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.ConsiderAttacking
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.TemplateMode
import Planning.Plans.Information.Employing
import Planning.Plans.Information.Reactive.EnemyBasesAtLeast
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtLeast, UnitsAtLeast}
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvP2GateRoboObs

class PvPOpen2GateRobo extends TemplateMode {
  
  override val activationCriteria: Plan = new Employing(PvP2GateRoboObs)
  
  override val completionCriteria: Plan = new Or(
    new EnemyBasesAtLeast(2),
    new UnitsAtLeast(2, Protoss.Nexus),
    new UnitsAtLeast(40, UnitMatchWarriors))
  
  override val buildOrder: Seq[BuildRequest] = Vector(
    // http://wiki.teamliquid.net/starcraft/2_Gate_Reaver_(vs._Protoss)
    // We get gas/core faster because of mineral locking + later scout
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),             // 8
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),           // 10
    RequestAtLeast(11,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),       // 11
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),            // 13
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),             // 16 = 14 + Z
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),   // 18 = 16 + Z
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Zealot),            // 19 = 17 + Z
    RequestAtLeast(18,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),             // 22 = 18 + ZZ
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Dragoon),           // 23 = 19 + ZZ
    RequestAtLeast(20,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Gateway),           // 26 = 20 + ZZ + D
    RequestAtLeast(21,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Dragoon),           // 27 = 21 + ZZ + D
    RequestAtLeast(22,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestUpgrade(Protoss.DragoonRange))
  
  override val buildPlans = Vector(
    new If(
      new EnemyUnitsAtLeast(1, Protoss.DarkTemplar),
      new Build(RequestAtLeast(1, Protoss.Observer))),
    new Trigger(new UnitsAtLeast(1, Protoss.Reaver), new RequireMiningBases(2)),
    new TrainContinuously(Protoss.Reaver),
    new TrainContinuously(Protoss.Dragoon),
    new Build(
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.RoboticsSupportBay),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(1, Protoss.Observer)))
  
  override val defaultScoutPlan: Plan = new If(new UnitsAtLeast(1, Protoss.CyberneticsCore), new Scout)
  
  override val defaultAttackPlan: Plan = new Trigger(
    new UnitsAtLeast(2, Protoss.Reaver, complete = true),
    initialAfter = new ConsiderAttacking)
}
