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
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Milestones.{BasesAtLeast, UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchTraining
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPGateGateRobo

class PvPGateGateRobo extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvPGateGateRobo)
  override val completionCriteria: Predicate =  new UnitsAtLeast(2, Protoss.Nexus)
  
  override def attackPlan: Plan = new PvP3GateRobo().attackPlan
  override def scoutPlan: Plan = new ScoutOn(Protoss.Pylon)

  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactToFFE,
    new PvPIdeas.ReactTo2Gate,
    new ScoutForCannonRush)
  
  override def buildOrder = Seq(
      // http://wiki.teamliquid.net/starcraft/2_Gate_Reaver_(vs._Protoss)
      Get(8,    Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10,   Protoss.Probe),
      Get(Protoss.Gateway),
      Get(12,   Protoss.Probe),
      Get(Protoss.Assimilator),
      Get(13,   Protoss.Probe),
      Get(Protoss.Zealot),
      Get(14,   Protoss.Probe),
      Get(2,    Protoss.Pylon),
      Get(16,   Protoss.Probe),
      Get(Protoss.CyberneticsCore),
      Get(17,   Protoss.Probe),
      Get(2,    Protoss.Zealot),
      Get(18,   Protoss.Probe),
      Get(3,    Protoss.Pylon),
      Get(19,   Protoss.Probe),
      Get(Protoss.Dragoon),
      Get(20,   Protoss.Probe),
      Get(2,    Protoss.Gateway),
      Get(21,   Protoss.Probe),
      Get(2,    Protoss.Dragoon),
      Get(4,    Protoss.Pylon),
      Get(Protoss.DragoonRange),
      Get(22,  Protoss.Probe),
      Get(4,   Protoss.Dragoon),
      Get(Protoss.RoboticsFacility))
  
  override val buildPlans = Vector(

    new EjectScout,

    new Trigger(
      new Or(
        new EnemyStrategy(With.fingerprints.nexusFirst),
        new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true),
        new And(
          new UnitsAtMost(0, Protoss.TemplarArchives),
          new UnitsAtLeast(1, Protoss.Shuttle, complete = true),
          new UnitsAtLeast(2, Protoss.Reaver, complete = true))),
      new RequireBases(2)),

    new If(
      new PvPIdeas.CanSkipObservers,
      new Parallel(
        new CancelIncomplete(Protoss.Observatory),
        new CancelOrders(UnitMatchTraining(Protoss.Observer))),
      new Pump(Protoss.Observer, 1)),
    new PumpShuttleAndReavers(6, shuttleFirst = false),
    new PvPIdeas.PumpDragoonsAndZealots,

    new FlipIf(
      new EnemyStrategy(
        With.fingerprints.cannonRush,
        With.fingerprints.proxyGateway,
        With.fingerprints.twoGate,
        With.fingerprints.fourGateGoon,
        With.fingerprints.nexusFirst,
        With.fingerprints.forgeFe,
        With.fingerprints.robo),
      new If(
        new Not(new PvPIdeas.CanSkipObservers),
        new BuildOrder(
          Get(Protoss.Observatory),
          Get(Protoss.Observer))),
      new BuildOrder(
        Get(Protoss.Shuttle),
        Get(Protoss.RoboticsSupportBay),
        Get(Protoss.Reaver))),

    new If(
      new EnemyStrategy(With.fingerprints.fourGateGoon),
      new Build(Get(3, Protoss.Gateway))),
    new RequireBases(2),
    new Trigger(
      new BasesAtLeast(2),
      new Build(
        Get(5, Protoss.Gateway),
        Get(2, Protoss.Assimilator))),
    new PumpWorkers(oversaturate = true)
  )
}
