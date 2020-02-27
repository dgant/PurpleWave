package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.Get
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.ReactToDarkTemplarEmergencies
import Planning.Plans.Macro.Automatic.{CapGasAt, CapGasWorkersAt, CapGasWorkersAtRatio, PumpWorkers}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtNatural, BuildCannonsInMain}
import Planning.Plans.Scouting.ScoutForCannonRush
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP1ZealotExpand

class PvP1ZealotExpand extends GameplanTemplate {

  private val defaultCannons: Int = 3
  override val activationCriteria: Predicate = new Employing(PvP1ZealotExpand)
  override val completionCriteria: Predicate = new Latch(new And(
    new BasesAtLeast(2),
    new UnitsAtLeast(5, Protoss.Gateway),
    new Or(
      new Not(new ShouldAddCannons),
      new UnitsAtLeast(defaultCannons, Protoss.PhotonCannon))))

  override def placementPlan: Plan = new Parallel(
    super.placementPlan,
    new If(
      new And(
        new BasesAtLeast(2),
        new UnitsAtLeast(2, Protoss.Pylon),
        new Not(new EnemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate))),
      new ProposePlacement {
        override lazy val blueprints = Vector(new Blueprint(this, building = Some(Protoss.Pylon), requireZone = Some(With.geography.ourNatural.zone)))
      }))

  override def attackPlan: Plan = new If(new EnemyStrategy(With.fingerprints.nexusFirst), new Attack)

  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactTo2Gate,
    new ScoutForCannonRush)

  override val buildOrderPlan = new Parallel(
    new BuildOrder(
      Get(8,  Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway),
      Get(13, Protoss.Probe),
      Get(Protoss.Zealot),
      Get(14, Protoss.Probe),
      Get(2,  Protoss.Pylon),
      Get(16, Protoss.Probe)),
    new If(
      new Not(new EnemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.cannonRush)),
      new BuildOrder(
        Get(2,  Protoss.Nexus),
        Get(17, Protoss.Probe))))

  override def workerPlan: Plan = new If(
    new Or(
      new UnitsAtLeast(5, Protoss.Gateway),
      new EnemyStrategy(With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.nexusFirst),
      new EnemyBasesAtLeast(2)),
    new PumpWorkers,
    new PumpWorkers(maximumTotal = 25))

  class ShouldAddCannons extends And(
    new BasesAtLeast(2),
    new Not(new EnemyBasesAtLeast(2)),
    new EnemiesAtMost(2, Protoss.Gateway),
    new Not(new EnemyStrategy(
      With.fingerprints.dragoonRange,
      With.fingerprints.twoGate,
      With.fingerprints.fourGateGoon,
      With.fingerprints.robo,
      With.fingerprints.forgeFe,
      With.fingerprints.gatewayFe)))

  override def buildPlans = Vector(
    // We really need very little gas early on;
    // rather, we depserately need minerals to get our Gateways and Forge up.
    new CapGasAt(250),
    new CapGasWorkersAtRatio(.14),
    new If(
      new And(
        new UnitsAtMost(1, Protoss.Gateway),
        new GasForUpgrade(Protoss.DragoonRange)),
      new CapGasWorkersAt(1),
      new If(
        new UnitsAtMost(4, Protoss.Gateway),
        new CapGasWorkersAt(2))),

    // Ordered to maximize chances of getting detection in time but only if absolutely necessary.
    // It's also quite possible to die to a 4Gate + DT if the ordering isn't careful.
    new If(
      new EnemiesAtLeast(2, Protoss.Zealot),
      new BuildOrder(Get(2, Protoss.Zealot))),
    new If(
      new EnemiesAtLeast(1, Protoss.CitadelOfAdun),
      new BuildCannonsAtNatural(1)),
    new If(
      new Or(
        new EnemiesAtLeast(1, Protoss.TemplarArchives),
        new EnemyHasShown(Protoss.DarkTemplar)),
      new Parallel(
        new BuildCannonsAtNatural(1),
        new BuildCannonsInMain(1),
        new BuildCannonsAtNatural(2))),
    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)),
    new ReactToDarkTemplarEmergencies,
    new Build(Get(2, Protoss.Gateway)),

    // If we don't know what they're doing, stick a Forge in there
    new If(new ShouldAddCannons, new Build(Get(Protoss.Forge))),

    // Finish the build order
    new Build(
      Get(3, Protoss.Gateway),
      Get(Protoss.DragoonRange)),
    new If(new ShouldAddCannons, new BuildCannonsAtNatural(defaultCannons)),
    new PvPIdeas.TrainArmy,
    new RequireMiningBases(2),
    new Build(
      Get(5, Protoss.Gateway),
      Get(Protoss.Forge)),
    new BuildGasPumps
  )
}
