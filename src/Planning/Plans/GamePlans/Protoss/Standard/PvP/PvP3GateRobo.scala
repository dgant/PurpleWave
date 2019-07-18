package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.EjectScout
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{Pump, PumpShuttleAndReavers, PumpWorkers}
import Planning.Plans.Macro.Build.{CancelIncomplete, CancelOrders}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireBases
import Planning.Plans.Scouting.{ScoutForCannonRush, ScoutOn}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.{UnitMatchTraining, UnitMatchWarriors}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP3GateRobo

class PvP3GateRobo extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvP3GateRobo)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(2, Protoss.Nexus))

  override def scoutPlan: Plan = new ScoutOn(Protoss.Gateway)
  override val attackPlan: Plan = new If(
    new Or(
      new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.gasSteal),
      new And(
        new EnemyStrategy(With.fingerprints.twoGate),
        new Or(
          new EnemyHasShown(Protoss.Gateway), // Don't abandon base vs. proxies
          new UnitsAtLeast(7, UnitMatchWarriors)),
        new UnitsAtLeast(1, Protoss.Dragoon, complete = true),
        new Or(
          new UpgradeComplete(Protoss.DragoonRange),
          new Not(new EnemyHasUpgrade(Protoss.DragoonRange)))),
      new And(
        new EnemyStrategy(With.fingerprints.dtRush),
        new UnitsAtLeast(2, Protoss.Observer, complete = true))),
    new PvPIdeas.AttackSafely)

  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactToFFE,
    new PvPIdeas.ReactTo2Gate,
    new ScoutForCannonRush)

  override def buildOrder = Seq(
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
    Get(4,   Protoss.Dragoon))

  override def buildPlans = Vector(

    new EjectScout,

    new If(
      new PvPIdeas.CanSkipObservers,
      new Parallel(
        new CancelIncomplete(Protoss.Observatory),
        new CancelOrders(UnitMatchTraining(Protoss.Observer)),
        new BuildOrder(
          Get(Protoss.Shuttle),
          Get(Protoss.RoboticsSupportBay))),
      new If(
        new EnemyHasShown(Protoss.DarkTemplar),
        new Parallel(
          new If(
            new UnitsAtMost(0, Protoss.Observer),
            new CancelOrders(UnitMatchTraining(Protoss.Reaver), UnitMatchTraining(Protoss.Shuttle))),
          new BuildOrder(
            Get(Protoss.Observatory),
            Get(Protoss.Observer))),
        new BuildOrder(
          Get(Protoss.Observatory),
          Get(Protoss.Shuttle),
          Get(Protoss.Observer),
          Get(Protoss.RoboticsSupportBay)))),

    new Trigger(
      new Or(
        new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true),
        new And(
          new UnitsAtLeast(12, Protoss.Dragoon),
          new SafeAtHome),
        new And(
          new UnitsAtMost(0, Protoss.TemplarArchives),
          new UnitsAtLeast(1, Protoss.Shuttle, complete = true),
          new UnitsAtLeast(4, Protoss.Reaver, complete = true))),
      new RequireBases(2)),

    new If(
      new PvPIdeas.CanSkipObservers,
      new CancelOrders(UnitMatchTraining(Protoss.Observer), Protoss.Observatory),
      new Pump(Protoss.Observer, 1)),
    new PumpShuttleAndReavers(6, shuttleFirst = false),
    new PvPIdeas.PumpDragoonsAndZealots,

    new PumpWorkers(oversaturate = true, 40),
    new RequireBases(2),
    new Trigger(
      new BasesAtLeast(2),
      new Build(
        Get(5, Protoss.Gateway),
        Get(2, Protoss.Assimilator)))
  )
}
