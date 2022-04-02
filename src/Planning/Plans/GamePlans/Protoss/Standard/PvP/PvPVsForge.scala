package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Buildables.Get
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.{Plan, Predicate}
import Planning.Predicates.Compound.{And, Latch, Not, Or}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyNaturalConfirmed
import Planning.Predicates.Strategy.{Employing, EnemyRecentStrategy, EnemyStrategy}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP3GateGoon

class PvPVsForge extends GameplanTemplate {

  // Bots have done a variety of nonsense behind FFE:
  // * Real FFE into macro
  // * Real FFE into Carrier rush
  // * Real FFE into DT rush
  // * Fake FFE into 3 proxy gates
  // This also needs to be robust against eg. Ximp turtling into Carriers

  override val activationCriteria: Predicate = And(
    EnemyStrategy(With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.cannonRush),
    UnitsAtMost(0, Protoss.CitadelOfAdun))
  override val completionCriteria: Predicate = Latch(BasesAtLeast(2))

  class ExpandASAP extends Parallel(new RequireMiningBases(2), new CapGasWorkersAt(0))

  val oneGateCoreLogic = new PvP1GateCoreLogic(allowZealotBeforeCore = false)
  override def emergencyPlans: Seq[Plan] = oneGateCoreLogic.emergencyPlans
  override def buildOrderPlan: Plan = new Parallel(
    new OldPvPIdeas.CancelAirWeapons,
    new If(
      Not(EnemyStrategy(With.fingerprints.cannonRush)),
      new Parallel(
        new If(UnitsAtMost(0, Protoss.CyberneticsCore), new CapGasWorkersAt(1)),

        // Vs. Gateway FE we need some Zealot production so they don't just walk into our mineral line
        new If(EnemyStrategy(With.fingerprints.gatewayFe),  new Parallel(new Build(Get(Protoss.Gateway)), new If(UnitsAtMost(0, Protoss.CyberneticsCore), new Pump(Protoss.Zealot)))),
        // If they've bought a Nexus, we can too
        new If(EnemyNaturalConfirmed(),                      new ExpandASAP),
        // If they've bought two cannons or gone Forge-first, we can buy a Nexus
        new If(EnemiesAtLeast(2, Protoss.PhotonCannon),     new ExpandASAP),
        new If(EnemyStrategy(With.fingerprints.forgeFe),    new ExpandASAP))),
    new (oneGateCoreLogic.BuildOrderPlan))

  override val buildPlans = Vector(
    new oneGateCoreLogic.WriteStatuses,
    new If(
      new GasCapsUntouched,
      new Parallel(
        new CapGasAt(200),
        new If(
          UnitsAtMost(0, Protoss.CyberneticsCore),
          new CapGasWorkersAt(1),
          new If(
            Not(UpgradeStarted(Protoss.DragoonRange)),
            new CapGasWorkersAt(2),
            new If(
              Employing(PvP3GateGoon),
              new If(UnitsAtMost(2, Protoss.Gateway), new CapGasWorkersAt(1)),
              new If(UnitsAtMost(3, Protoss.Gateway), new CapGasWorkersAt(1))))))),

    new BuildOrder(Get(Protoss.DragoonRange),  Get(2, Protoss.Dragoon)),
    new If(
      Employing(PvP3GateGoon),
      new BuildOrder(Get(3, Protoss.Gateway), Get(8, Protoss.Dragoon)),
      new BuildOrder(Get(4, Protoss.Gateway), Get(10, Protoss.Dragoon))),

    new If(
      Or( // All smells of DT risk
        EnemyRecentStrategy(With.fingerprints.dtRush),
        EnemiesAtLeast(1, Protoss.Forge),
        EnemiesAtLeast(1, Protoss.PhotonCannon)),
      new Build(Get(Protoss.Forge))),

    new If(
      Or(
        And(
          Employing(PvP3GateGoon),
          UnitsAtLeast(3, Protoss.Gateway, complete = true)),
        And(
          UnitsAtLeast(16, Protoss.Dragoon),
          Not(EnemyStrategy(With.fingerprints.fourGateGoon)))),
      new RequireMiningBases(2)),

    new Pump(Protoss.Dragoon),
    new Build(Get(4, Protoss.Gateway)),
    new PumpWorkers(oversaturate = true)
  )
}
