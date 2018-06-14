package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
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
  
  override val activationCriteria: Predicate = new Employing(PvPOpen2GateRobo)
  override val completionCriteria: Predicate = new Or(
    new EnemyBasesAtLeast(2),
    new UnitsAtLeast(2, Protoss.Nexus),
    new UnitsAtLeast(40, UnitMatchWarriors))
  
  override def defaultAttackPlan  : Plan    = new PvPIdeas.AttackSafely
  override val scoutAt            : Int     = 14
  override def aggression         : Double  = 0.85

  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToFFE,
    new PvPIdeas.ReactToTwoGate
  )
  
  override val buildOrder: Seq[BuildRequest] = Vector(
    // http://wiki.teamliquid.net/starcraft/2_Gate_Reaver_(vs._Protoss)
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),             // 8
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Gateway),           // 10
    Get(12,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),       // 12
    Get(13,  Protoss.Probe),
    Get(1,   Protoss.Zealot),            // 13
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.Pylon),             // 16 = 14 + Z
    Get(16,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore),   // 18 = 16 + Z
    Get(17,  Protoss.Probe),
    Get(2,   Protoss.Zealot),            // 19 = 17 + Z
    Get(18,  Protoss.Probe),
    Get(3,   Protoss.Pylon),             // 22 = 18 + ZZ
    Get(19,  Protoss.Probe),
    Get(1,   Protoss.Dragoon),           // 23 = 19 + ZZ
    Get(20,  Protoss.Probe),
    Get(2,   Protoss.Gateway),           // 26 = 20 + ZZ + D
    Get(21,  Protoss.Probe),
    Get(2,   Protoss.Dragoon),           // 27 = 21 + ZZ + D
    Get(22,  Protoss.Probe),
    Get(3,   Protoss.Pylon),
    Get(Protoss.DragoonRange))
  
  override val buildPlans = Vector(
    new If(
      new EnemiesAtLeast(1, Protoss.DarkTemplar),
      new Build(Get(1, Protoss.Observer))),
    new Trigger(new UnitsAtLeast(1, Protoss.Reaver), new RequireMiningBases(2)),
    new PvPIdeas.TrainArmy,
    new Build(
      Get(1, Protoss.RoboticsFacility),
      Get(1, Protoss.Observatory),
      Get(1, Protoss.RoboticsSupportBay),
      Get(3, Protoss.Gateway),
      Get(2, Protoss.Nexus),
      Get(1, Protoss.Observer)))
}
