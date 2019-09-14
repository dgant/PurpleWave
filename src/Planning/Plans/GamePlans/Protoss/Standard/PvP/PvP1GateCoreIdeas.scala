package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Compound.{If, Or, Parallel}
import Planning.Plans.Macro.Build.CancelIncomplete
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Scouting.{ScoutForCannonRush, ScoutOn}
import Planning.Predicates.Compound.{And, Check, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyDarkTemplarLikely
import Planning.Predicates.Strategy._
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPRobo

object PvP1GateCoreIdeas {

  class ScoutPlan extends If(new StartPositionsAtLeast(3), new ScoutOn(Protoss.Gateway), new ScoutOn(Protoss.CyberneticsCore))

  class GateGate extends And(
    new Employing(PvPRobo),
    new Or(
      new Latch(
        new And(
          new EnemyStrategy(
            With.fingerprints.nexusFirst,
            With.fingerprints.gatewayFe,
            With.fingerprints.twoGate,
            With.fingerprints.proxyGateway),
          new UnitsAtMost(0, Protoss.RoboticsFacility))),
      new Check(() => With.strategy.isFlat || With.strategy.isInverted)))

  class PossibleZealotPressure extends Not(new EnemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore))

  class ZealotBeforeCore extends And(
    new PossibleZealotPressure,
    new Or(
      new StartPositionsAtMost(2),
      new EnemyRecentStrategy(
        With.fingerprints.workerRush,
        With.fingerprints.mannerPylon,
        With.fingerprints.cannonRush,
        With.fingerprints.proxyGateway,
        With.fingerprints.twoGate,
        With.fingerprints.nexusFirst)))

  class ZealotAfterCore extends Or(
    new EnemyStrategy(
      With.fingerprints.workerRush,
      With.fingerprints.mannerPylon,
      With.fingerprints.proxyGateway,
      With.fingerprints.twoGate),
    new And(
      new PossibleZealotPressure,
      new Not(new GateGate))) // We want that second Gateway up quickly instead

  def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactTo2Gate,
    new ScoutForCannonRush,
    new If(
      new EnemyDarkTemplarLikely,
      new If(
        new Latch(new UnitsAtMost(0, Protoss.CyberneticsCore)),
        new PvPIdeas.ReactToDarkTemplarEmergencies,
        new Parallel(
          new If(new UnitsAtMost(0, Protoss.Observatory), new CancelIncomplete(Protoss.RoboticsSupportBay)),
          new If(new UnitsAtMost(0, Protoss.Observer), new CancelIncomplete(Protoss.Shuttle, Protoss.Reaver)),
          new BuildOrder(
            Get(Protoss.RoboticsFacility),
            Get(Protoss.Observatory),
            Get(2, Protoss.Observer))))))

  class BuildOrderPlan(allowZealotBeforeCore: Boolean = true) extends Parallel(
    new BuildOrder(
      Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway),
      Get(12, Protoss.Probe),
      Get(Protoss.Assimilator),
      Get(13, Protoss.Probe)),
    new If(
      new And(new ZealotBeforeCore, new Check(() => allowZealotBeforeCore)),

      // ZCore*
      new Parallel(
        new BuildOrder(
          Get(Protoss.Zealot),
          Get(14, Protoss.Probe),
          Get(2, Protoss.Pylon),
          Get(15, Protoss.Probe),
          Get(Protoss.CyberneticsCore),
          Get(16, Protoss.Probe)),

        new If(
          new ZealotAfterCore,
          // ZCoreZ
          new BuildOrder(Get(2, Protoss.Zealot)))),

      // Core*
      new Parallel(
        new BuildOrder(
          Get(14, Protoss.Probe),
          Get(Protoss.CyberneticsCore)),

        new If(
          new ZealotAfterCore,
          // CoreZ
          new BuildOrder(
            Get(Protoss.Zealot),
            Get(2, Protoss.Pylon),
            Get(16, Protoss.Probe)),

          // NZCore
          new BuildOrder(
            Get(15, Protoss.Probe),
            Get(2, Protoss.Pylon),
            Get(17, Protoss.Probe)))
      )))
}