package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Basic.WriteStatus
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.ReactToDarkTemplarEmergencies
import Planning.Plans.Macro.Build.CancelIncomplete
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Scouting.ScoutForCannonRush
import Planning.Predicates.Compound._
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyDarkTemplarLikely
import Planning.Predicates.Strategy._
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPRobo

class PvP1GateCoreLogic(allowZealotBeforeCore: Boolean = true, requireZealotBeforeCore: Boolean = false) {

  class PossibleZealotPressure extends Not(EnemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore))

  class WriteStatuses extends Parallel(
    new If(new GateGate, new WriteStatus("GateGate"), new If(new GateTechGateGate, new WriteStatus("GateTechGateGate"))),
    new If(new PossibleZealotPressure, new WriteStatus("PossibleZealotPressure")),
    new If(
      new ZealotBeforeCore,
      new If(
        new ZealotAfterCore,
        new WriteStatus("ZCoreZ"),
        new WriteStatus("ZCore")),
      new If(
        new ZealotAfterCore,
        new WriteStatus("CoreZ"),
        new WriteStatus("NZCore"))))

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

  class GateTechGateGate extends And(
    new Employing(PvPRobo),
    new Not(new GateGate),
    new Check(() => With.strategy.isInverted))

  class ZealotBeforeCore extends Or(
    // If we've definitely started with ZCore*
    new Check(() => requireZealotBeforeCore),
    new Latch(
      new And(
        new UnitsAtLeast(1, Protoss.Zealot),
        new UnitsAtMost(0, Protoss.CyberneticsCore))),
    new And(
      new Check(() => allowZealotBeforeCore),
      // If we haven't committed to Core*
      new Or(
        new UnitsAtMost(0, Protoss.CyberneticsCore),
        new UnitsAtLeast(1, Protoss.Zealot)),
      new Or(
        new EnemyRecentStrategy(
          With.fingerprints.gasSteal,
          With.fingerprints.mannerPylon,
          With.fingerprints.workerRush,
          With.fingerprints.cannonRush,
          With.fingerprints.nexusFirst),
        new And(
          new PossibleZealotPressure,
          new EnemyRecentStrategy(
            With.fingerprints.proxyGateway,
            With.fingerprints.twoGate)))))

  class ZealotAfterCore extends Or(
    new Latch(
      new And(
        new Not(new ZealotBeforeCore),
        new UnitsAtLeast(1, Protoss.CyberneticsCore),
        new UnitsAtLeast(1, Protoss.Zealot))),
    new Latch(
      new And(
        new UnitsAtLeast(2, Protoss.Zealot),
        new UnitsAtLeast(1, Protoss.CyberneticsCore))),
    new EnemyStrategy(
      With.fingerprints.workerRush,
      With.fingerprints.mannerPylon,
      With.fingerprints.proxyGateway,
      With.fingerprints.twoGate,
      With.fingerprints.gasSteal),
    new And(
      new PossibleZealotPressure,
      new Not(new GateGate))) // We want that second Gateway up quickly instead

  def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    // We don't need to do anything special against standard 2-gate
    // new PvPIdeas.ReactTo2Gate,
    new ScoutForCannonRush,
    new If(
      new Employing(PvPRobo),
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
              Get(2, Protoss.Observer))))),
      new ReactToDarkTemplarEmergencies))

  class BuildOrderPlan extends Parallel(
    new BuildOrder(
      Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway),
      Get(12, Protoss.Probe)),
    new If(
      new ZealotBeforeCore,

      // ZCore*
      new Parallel(
        new BuildOrder(
          Get(2, Protoss.Pylon),
          Get(13, Protoss.Probe),
          Get(Protoss.Zealot),
          Get(14, Protoss.Probe),
          Get(Protoss.Assimilator),
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
          Get(Protoss.Assimilator),
          Get(13, Protoss.Probe),
          Get(Protoss.CyberneticsCore),
          Get(14, Protoss.Probe)),
        new If(
          new ZealotAfterCore,
          //CoreZ
          new BuildOrder(
            Get(Protoss.Zealot),
            Get(2, Protoss.Pylon),
            Get(16, Protoss.Probe)),
          // NZCore
          new BuildOrder(
            Get(15, Protoss.Probe),
            Get(2, Protoss.Pylon),
            Get(17, Protoss.Probe))))))
}