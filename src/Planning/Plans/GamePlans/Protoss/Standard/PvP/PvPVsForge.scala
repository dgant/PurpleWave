package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Buildables.Get
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, Pump}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.{Plan, Predicate}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyNaturalConfirmed
import Planning.Predicates.Strategy.EnemyStrategy
import ProxyBwapi.Races.Protoss

class PvPVsForge extends PvP34GateGoon {

  // Bots have done a variety of nonsense behind FFE:
  // * Real FFE into macro
  // * Real FFE into Carrier rush
  // * Real FFE into DT rush
  // * Fake FFE into 3 proxy gates
  // This also needs to be robust against eg. Ximp turtling into Carriers

  override val activationCriteria: Predicate = new And(
    new EnemyStrategy(With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.cannonRush),
    new UnitsAtMost(0, Protoss.CitadelOfAdun))

  override val completionCriteria: Predicate = new Latch(new BasesAtLeast(2))

  class ExpandASAP extends Parallel(new RequireMiningBases(2), new CapGasWorkersAt(0))

  override def buildOrderPlan: Plan = new Parallel(
    new OldPvPIdeas.CancelAirWeapons,
    new If(
      new Not(new EnemyStrategy(With.fingerprints.cannonRush)),
      new Parallel(
        new If(new UnitsAtMost(0, Protoss.CyberneticsCore), new CapGasWorkersAt(1)),

        // Vs. Gateway FE we need some Zealot production so they don't just walk into our mineral line
        new If(new EnemyStrategy(With.fingerprints.gatewayFe),  new Parallel(new Build(Get(Protoss.Gateway)), new If(new UnitsAtMost(0, Protoss.CyberneticsCore), new Pump(Protoss.Zealot)))),
        // If they've bought a Nexus, we can too
        new If(new EnemyNaturalConfirmed,                       new ExpandASAP),
        // If they've bought two cannons or gone Forge-first, we can buy a Nexus
        new If(new EnemiesAtLeast(2, Protoss.PhotonCannon),     new ExpandASAP),
        new If(new EnemyStrategy(With.fingerprints.forgeFe),    new ExpandASAP))),
    super.buildOrderPlan
  )
}
