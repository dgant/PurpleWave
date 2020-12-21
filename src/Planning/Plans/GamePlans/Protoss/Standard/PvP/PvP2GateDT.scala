package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.CancelOrders
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Placement.{BuildCannonsAtNatural, BuildCannonsInMain, ProposePlacement}
import Planning.Plans.Scouting.{ScoutCleared, ScoutForCannonRush}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, EnemyDarkTemplarLikely, SafeAtHome}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP2GateDTExpand
import Utilities.GameTime

class PvP2GateDT extends GameplanTemplate {
  
  override val activationCriteria = new Employing(PvP2GateDTExpand)
  override val completionCriteria: Predicate = new Latch(new And(new UnitsAtLeast(1, Protoss.TemplarArchives), new BasesAtLeast(2)))

  override def priorityAttackPlan: Plan = new Attack(Protoss.DarkTemplar)

  override val attackPlan = new If(
    new Or(
      new Latch(new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true)),
      new EnemyBasesAtLeast(2)),
    new Attack)

  override def placementPlan: Plan = new Parallel(
    super.placementPlan,
    new If(
      new Or(
        new BasesAtLeast(2),
        new UnitsAtLeast(1, Protoss.DarkTemplar),
        new Not(new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway))),
      new ProposePlacement {
        override lazy val blueprints = Vector(new Blueprint(Protoss.Pylon, requireZone = Some(With.geography.ourNatural.zone)))
      }))

  override def blueprints = Vector(
    new Blueprint(Protoss.Pylon,           placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 10.0)),
    new Blueprint(Protoss.Gateway,         placement = Some(PlacementProfiles.wallGathering)),
    new Blueprint(Protoss.Pylon,           placement = Some(PlacementProfiles.wallGathering)),
    new Blueprint(Protoss.ShieldBattery),
    new Blueprint(Protoss.Gateway,         placement = Some(PlacementProfiles.wallGathering)),
    new Blueprint(Protoss.CyberneticsCore, placement = Some(PlacementProfiles.wallGathering)),
    new Blueprint(Protoss.Pylon,           placement = Some(PlacementProfiles.backPylon)),
    new Blueprint(Protoss.Forge,           placement = Some(PlacementProfiles.wallGathering)),
    new Blueprint(Protoss.Pylon,           placement = Some(PlacementProfiles.wallGathering)))

  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToCannonRush,
    new If(
      new UnitsAtMost(0, Protoss.CitadelOfAdun),
      new Parallel(
        new PvPIdeas.ReactToProxyGateways,
        new PvPIdeas.ReactTo2Gate)),
    new PvPIdeas.ReactToFFE,
    new ScoutForCannonRush)

  val oneGateCoreLogic = new PvP1GateCoreLogic(allowZealotBeforeCore = false)

  override def buildOrderPlan = new oneGateCoreLogic.BuildOrderPlan
  
  override val buildPlans = Vector(
    new Trigger(
      new UnitsAtLeast(1, Protoss.Dragoon, complete = true),
      new EjectScout,
      new EjectScout(Protoss.Probe)),

    new If(
      new GasCapsUntouched,
      new Trigger(
        new UnitsAtLeast(1, Protoss.CitadelOfAdun),
        new CapGasAt(300),
        new CapGasAt(200))),

    new oneGateCoreLogic.WriteStatuses,

    new If(
      new And(
        new UpgradeStarted(Protoss.AirDamage),
        new EnemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate)),
      new CancelOrders(Protoss.CyberneticsCore)),

    // Delay build until scout cleared
    new If(
      new Or(
        new UnitsAtLeast(1, Protoss.CitadelOfAdun),
        new ScoutCleared,
        new FrameAtLeast(GameTime(4, 10)())),

      new Parallel(
        // Don't accidentally cancel range that we actually want/need
        new If(
          new Not(new EnemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate)),
          new CancelOrders(Protoss.CyberneticsCore)),
        new BuildOrder(
          Get(Protoss.Dragoon),
          Get(Protoss.CitadelOfAdun),
          Get(2, Protoss.Dragoon),
          Get(Protoss.TemplarArchives)),
        new If(
          new And(
            new EnemiesAtLeast(2, Protoss.PhotonCannon),
            new Not(new EnemyStrategy(With.fingerprints.cannonRush, With.fingerprints.dtRush))),
          new RequireMiningBases(2),
          new Build(Get(2, Protoss.Gateway))),
        new BuildOrder(
          Get(2, Protoss.DarkTemplar),
          Get(Protoss.Forge)),
        new If(
          new And(
            new EnemiesAtMost(0, Protoss.Observer),
            new EnemiesAtMost(0, Protoss.Observatory),
            new UnitsAtMost(2, Protoss.DarkTemplar)),
            new Pump(Protoss.DarkTemplar, 3, 1)),
        new If(
          new EnemyDarkTemplarLikely,
          new Parallel(
            new BuildCannonsInMain(1),
            new BuildCannonsAtNatural(2),
            new BuildCannonsInMain(2),
            new BuildCannonsAtNatural(3))),
        new BuildCannonsAtNatural(0),
        new If(
          new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)),
          new BuildCannonsAtNatural(1)),
        new If(new UnitsAtLeast(12, Protoss.Dragoon), new RequireMiningBases(2)),
        new If(
          new And(
            new EnemyStrategy(With.fingerprints.twoGate),
            new Not(new SafeAtHome),
            new UnitsAtMost(12, UnitMatchWarriors)),
          new Pump(Protoss.Dragoon)),
        new If(
          new EnemyStrategy(With.fingerprints.proxyGateway),
          new Build(Get(4, Protoss.Gateway)),
          new Parallel(
            new BuildCannonsAtNatural(0),
            new RequireMiningBases(2))),
        new If(new EnemyStrategy(With.fingerprints.dtRush), new BuildCannonsAtNatural(3)), // Get a LOT so they dont get bursted down
        new If(
          new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)),
          new BuildCannonsAtNatural(2))),

    // Do while ejecting scout
    new Parallel(
      new BuildOrder(Get(Protoss.Dragoon)),
      new If(
        new Not(new EnemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate)),
        new Build(Get(Protoss.AirDamage))),
      new BuildOrder(
        Get(2, Protoss.Gateway),
        Get(2, Protoss.Dragoon)))),

    new PumpWorkers(oversaturate = true),
    new Pump(Protoss.Dragoon),

    // Include this in case the Archives is heavily delayed for whatever reason
    new RequireMiningBases(2),
    new Build(Get(8, Protoss.Gateway))
  )
}
