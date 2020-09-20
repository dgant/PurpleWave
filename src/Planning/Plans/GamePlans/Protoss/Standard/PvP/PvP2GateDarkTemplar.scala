package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Information.Fingerprinting.Generic.GameTime
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
import Planning.Predicates.Reactive.{EnemyDarkTemplarLikely, SafeAtHome}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP2GateDTExpand

class PvP2GateDarkTemplar extends GameplanTemplate {
  
  override val activationCriteria = new Employing(PvP2GateDTExpand)
  override val completionCriteria: Predicate = new Latch(new And(new UnitsAtLeast(1, Protoss.TemplarArchives), new BasesAtLeast(2)))

  override def priorityAttackPlan: Plan = new Attack(Protoss.DarkTemplar)

  override val attackPlan = new If(
    new Or(
      // It's our timing
      new Latch(new UnitsAtLeast(1, Protoss.DarkTemplar)),
      new And(
        new FoundEnemyBase,
        // Attack greedy openings
        new EnemyStrategy(With.fingerprints.nexusFirst),
        // Pressure proxy opening
        new Or(
          new Not(new EnemyStrategy(With.fingerprints.proxyGateway)),
          new UnitsAtLeast(1, Protoss.Dragoon, complete = true)))),
    new Attack,
    new PvPIdeas.AttackSafely)

  override def placementPlan: Plan = new Parallel(
    super.placementPlan,
    new If(
      new And(
        new UnitsAtLeast(1, Protoss.CitadelOfAdun),
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

  override val buildOrder = Vector(
    // http://wiki.teamliquid.net/starcraft/2_Gateway_Dark_Templar_(vs._Protoss)
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),                 // 8
    Get(10,  Protoss.Probe),
    Get(Protoss.Gateway),               // 10
    Get(12,  Protoss.Probe),
    Get(Protoss.Assimilator),           // 12
    Get(13,  Protoss.Probe),
    Get(Protoss.Zealot),                // 13
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.Pylon),            // 16
    Get(15,  Protoss.Probe),
    Get(Protoss.CyberneticsCore),       // 17
    Get(16,  Protoss.Probe),
    Get(2,   Protoss.Zealot),           // 18 = 16 + Z
    Get(17,  Protoss.Probe),
    Get(3,   Protoss.Pylon),            // 21 = 17 + ZZ
    Get(18,  Protoss.Probe),
    Get(Protoss.Dragoon),               // 22 = 18 + ZZ
    Get(Protoss.CitadelOfAdun),         // 24
    Get(20,  Protoss.Probe),
    Get(2,   Protoss.Dragoon),          // 26
    Get(2,   Protoss.Gateway),          // 28
    Get(21,  Protoss.Probe),
    Get(Protoss.TemplarArchives),
    // Classic build gets 2 Zealots -- can we fit in? do we want to?
    Get(22,  Protoss.Probe),
    Get(4,   Protoss.Pylon),
    Get(23,  Protoss.Probe),
    Get(2,   Protoss.DarkTemplar),
    Get(24,  Protoss.Probe),
    Get(Protoss.Forge),
    Get(25,  Protoss.Probe),
    Get(5,   Protoss.Pylon))

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

  override def buildOrderPlan = new PvP1GateCoreIdeas.BuildOrderPlan
  
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
          new RequireMiningBases(2)),
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
