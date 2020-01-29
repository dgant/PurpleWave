package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicate
import Planning.Predicates.Compound.{And, Latch}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyNaturalConfirmed
import Planning.Predicates.Strategy.EnemyStrategy
import ProxyBwapi.Races.Protoss

class PvPVsForgeNew extends PvP3GateGoon {

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

  override def buildOrderPlan = new Parallel(
    new PvPIdeas.CancelAirWeapons,
    new If(
      new EnemyStrategy(With.fingerprints.cannonRush),
      new Build(
        Get(Protoss.Assimilator),
        Get(Protoss.CyberneticsCore),
        Get(Protoss.RoboticsFacility),
        Get(2, Protoss.Reaver)),
      new Parallel(
        new If(new EnemyNaturalConfirmed, new RequireMiningBases(2)),
        new If(new EnemiesAtLeast(2, Protoss.PhotonCannon), new RequireMiningBases(2)),
        new If(new EnemyStrategy(With.fingerprints.forgeFe), new RequireMiningBases(2)))),
    super.buildOrderPlan
  )
}
