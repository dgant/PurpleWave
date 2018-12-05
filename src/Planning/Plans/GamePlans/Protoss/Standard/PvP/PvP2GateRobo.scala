package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.EjectScout
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch}
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen2GateRobo

class PvP2GateRobo extends GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Employing(PvPOpen2GateRobo)
  override val completionCriteria: Predicate = new Latch(
    new And(
      new UnitsAtLeast(2, Protoss.Nexus),
      new UnitsAtLeast(1, Protoss.RoboticsSupportBay),
      new UnitsAtLeast(5, Protoss.Gateway)))
  
  override def defaultAttackPlan  : Plan    = new If(new EnemyStrategy(With.fingerprints.nexusFirst), new PvPIdeas.AttackSafely)
  override def defaultScoutPlan   : Plan    = new ScoutOn(Protoss.Pylon)
  override def aggression         : Double  = 0.85

  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToCannonRush,
    new Trigger(new UnitsAtLeast(1, Protoss.Reaver, complete = true), new PvPIdeas.ReactToFFE),
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactToTwoGate)
  
  override val buildOrder: Seq[BuildRequest] = Vector(
    // http://wiki.teamliquid.net/starcraft/2_Gate_Reaver_(vs._Protoss)
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),            // 8
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Gateway),          // 10
    Get(12,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),      // 12
    Get(13,  Protoss.Probe),
    Get(1,   Protoss.Zealot),           // 13
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.Pylon),            // 16 = 14 + Z
    Get(16,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore),  // 18 = 16 + Z
    Get(17,  Protoss.Probe),
    Get(2,   Protoss.Zealot),           // 19 = 17 + Z
    Get(18,  Protoss.Probe),
    Get(3,   Protoss.Pylon),            // 22 = 18 + ZZ (25 -> 33)
    Get(19,  Protoss.Probe),
    Get(1,   Protoss.Dragoon),          // 23 = 19 + ZZ
    Get(20,  Protoss.Probe),
    Get(2,   Protoss.Gateway),          // 26 = 20 + ZZ + D
    Get(21,  Protoss.Probe),
    Get(2,   Protoss.Dragoon),          // 27 = 21 + ZZ + D
    Get(22,  Protoss.Probe),
    Get(4,   Protoss.Pylon),            // 30 = 22 + ZZ + DD (33 -> 41)
    Get(24,  Protoss.Probe),
    Get(4,   Protoss.Dragoon),          // 32 = 24 + ZZ + DD
    Get(25,  Protoss.Probe),
    Get(Protoss.RoboticsFacility),      // 37 = 25 + ZZ + DDDD
    Get(5,   Protoss.Pylon),            // 37 = 25 + ZZ + DDDD (41 -> 49)
    Get(6,   Protoss.Dragoon),
    Get(26,  Protoss.Probe),
    Get(Protoss.DragoonRange),          // 42 = 26 + 2Z + 6D
    Get(27,  Protoss.Probe),
    Get(8,   Protoss.Dragoon),
    Get(6,   Protoss.Pylon),            // 47 = 27 + 2Z + 8D (49 -> 57)
    Get(Protoss.Observatory),           // 47 = 27 + 2Z + 8D
    Get(29,  Protoss.Probe),
    Get(Protoss.Observer),              // 49 = 29 + 2Z + 8D
    Get(7,   Protoss.Pylon),            // 50 = 27 + 2Z + 8D + Obs (57 -> 65)
    Get(10,  Protoss.Dragoon),
    Get(Protoss.Shuttle),               // 54 = 29 + 2Z + 10D + Obs
    Get(12,  Protoss.Dragoon),
    Get(Protoss.RoboticsSupportBay),    // 58
    Get(15,  Protoss.Dragoon),
    Get(7,   Protoss.Pylon),            // 62 = 27 + 2Z + 8D + Obs (57 -> 65)
  )
  
  override val buildPlans = Vector(

    new EjectScout,

    new If(
      new Or(
        new EnemyStrategy(With.fingerprints.nexusFirst),
        new UnitsAtLeast(3, Protoss.Reaver),
        new And(
          new EnemyStrategy(With.fingerprints.dtRush),
          new UnitsAtLeast(2, Protoss.Observer, complete = true))),
      new RequireMiningBases(2)),

    new PvPIdeas.TrainArmy,

    new Build(
      Get(5, Protoss.Gateway),
      Get(2, Protoss.Assimilator))
  )
}
