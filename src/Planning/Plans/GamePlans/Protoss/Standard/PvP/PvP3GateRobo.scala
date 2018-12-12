package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.EjectScout
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.PumpWorkers
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{RequireBases, RequireMiningBases}
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchOr
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen3GateRobo

class PvP3GateRobo extends GameplanModeTemplate {

  override val activationCriteria: Predicate = new Employing(PvPOpen3GateRobo)
  override val completionCriteria: Predicate = new Latch(
    new And(new UnitsAtLeast(2, Protoss.Nexus), new UnitsAtLeast(5, Protoss.Gateway)))

  override def defaultScoutPlan: Plan = new ScoutOn(Protoss.Gateway)
  override val defaultAttackPlan: Plan = new If(new EnemyStrategy(With.fingerprints.nexusFirst), new PvPIdeas.AttackSafely)

  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactToFFE)

  override def defaultBuildOrder: Plan = new Parallel(
    new BuildOrder(
      Get(8,   Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10,  Protoss.Probe),
      Get(Protoss.Gateway),
      Get(12,  Protoss.Probe),
      Get(Protoss.Assimilator),
      Get(14,  Protoss.Probe),
      Get(Protoss.CyberneticsCore),
      Get(1,   Protoss.Zealot),
      Get(2,   Protoss.Pylon),
      Get(16,  Protoss.Probe),
      Get(1,   Protoss.Dragoon),
      Get(Protoss.DragoonRange),
      Get(17,  Protoss.Probe),
      Get(3,   Protoss.Pylon),
      Get(18,  Protoss.Probe),
      Get(2,   Protoss.Dragoon),
      Get(20,  Protoss.Probe),
      Get(Protoss.RoboticsFacility),
      Get(21,  Protoss.Probe),
      Get(3,   Protoss.Dragoon),
      Get(3,   Protoss.Gateway),
      Get(4,   Protoss.Dragoon),
      Get(22,  Protoss.Probe),
      Get(4,   Protoss.Pylon)))


  override def buildPlans = Vector(

    new EjectScout,

    new If(
      new EnemyStrategy(With.fingerprints.fourGateGoon),
      new BuildOrder(
        Get(Protoss.CitadelOfAdun),
        Get(Protoss.TemplarArchives)),
      new If(
        new EnemyStrategy(With.fingerprints.robo),
        new Build(
          Get(Protoss.Shuttle),
          Get(Protoss.RoboticsSupportBay)),
        new BuildOrder(
          Get(Protoss.Observatory),
          Get(Protoss.Observer),
          Get(Protoss.Shuttle),
          Get(Protoss.RoboticsSupportBay)))),

    new Trigger(
      new And(
        new UnitsAtLeast(1, UnitMatchOr(Protoss.RoboticsSupportBay, Protoss.TemplarArchives)),
        new Or(
          new UnitsAtMost(0, Protoss.TemplarArchives),
          new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true)),
        new Or(
          new UnitsAtMost(0, Protoss.RoboticsSupportBay),
          new And(
            new UnitsAtLeast(1, Protoss.Shuttle, complete = true),
            new UnitsAtLeast(1, Protoss.Reaver, complete = true)))),
      new RequireBases(2)),

    new PvPIdeas.TrainArmy,

    new PumpWorkers(oversaturate = true, 40),
    new RequireMiningBases(2),
    new Build(
      Get(5, Protoss.Gateway),
      Get(2, Protoss.Assimilator),
      Get(Protoss.Observatory),
      Get(Protoss.RoboticsSupportBay))
  )
}
