package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.{BuildRequest, GetAtLeast, GetUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.{EnemiesAtLeast, UnitsAtLeast}
import Planning.Plans.Predicates.Reactive.EnemyBasesAtLeast
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen2GateRobo

class PvP2GateRobo extends GameplanModeTemplate {
  
  override val activationCriteria : Plan    = new Employing(PvPOpen2GateRobo)
  override def defaultAttackPlan  : Plan    = new PvPIdeas.AttackSafely
  override val scoutAt            : Int     = 14
  override def aggression         : Double  = 0.85
  override val completionCriteria : Plan = new Or(
    new EnemyBasesAtLeast(2),
    new UnitsAtLeast(2, Protoss.Nexus),
    new UnitsAtLeast(40, UnitMatchWarriors))
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToFFE,
    new PvPIdeas.ReactToTwoGate
  )
  
  override val buildOrder: Seq[BuildRequest] = Vector(
    // http://wiki.teamliquid.net/starcraft/2_Gate_Reaver_(vs._Protoss)
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),             // 8
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),           // 10
    GetAtLeast(12,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),       // 12
    GetAtLeast(13,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Zealot),            // 13
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon),             // 16 = 14 + Z
    GetAtLeast(16,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),   // 18 = 16 + Z
    GetAtLeast(17,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Zealot),            // 19 = 17 + Z
    GetAtLeast(18,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Pylon),             // 22 = 18 + ZZ
    GetAtLeast(19,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Dragoon),           // 23 = 19 + ZZ
    GetAtLeast(20,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Gateway),           // 26 = 20 + ZZ + D
    GetAtLeast(21,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Dragoon),           // 27 = 21 + ZZ + D
    GetAtLeast(22,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Pylon),
    GetUpgrade(Protoss.DragoonRange))
  
  override val buildPlans = Vector(
    new If(
      new EnemiesAtLeast(1, Protoss.DarkTemplar),
      new Build(GetAtLeast(1, Protoss.Observer))),
    new Trigger(new UnitsAtLeast(1, Protoss.Reaver), new RequireMiningBases(2)),
    new PvPIdeas.TrainArmy,
    new Build(
      GetAtLeast(1, Protoss.RoboticsFacility),
      GetAtLeast(1, Protoss.Observatory),
      GetAtLeast(1, Protoss.RoboticsSupportBay),
      GetAtLeast(3, Protoss.Gateway),
      GetAtLeast(2, Protoss.Nexus),
      GetAtLeast(1, Protoss.Observer)))
}
