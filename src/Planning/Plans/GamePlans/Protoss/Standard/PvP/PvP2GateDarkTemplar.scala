package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.{Plan, Predicate}
import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.{CancelOrders, ProposePlacement}
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.{BuildCannonsAtNatural, BuildCannonsInMain}
import Planning.Plans.Scouting.{ScoutCleared, ScoutForCannonRush}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyDarkTemplarLikely, SafeAtHome}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP2GateDTExpand

class PvP2GateDarkTemplar extends GameplanTemplate {
  
  override val activationCriteria = new Employing(PvP2GateDTExpand)
  override val completionCriteria: Predicate = new Latch(new BasesAtLeast(2))

  override val scoutPlan = new PvP1GateCoreIdeas.ScoutPlan

  override def priorityAttackPlan: Plan = new Attack(Protoss.DarkTemplar)

  override val attackPlan  = new Trigger(
    new Or(
      new UnitsAtLeast(1, Protoss.DarkTemplar),
      new And(
        new UpgradeStarted(Protoss.DragoonRange),
        new Not(new EnemyStrategy(With.fingerprints.proxyGateway)),
        new PvPIdeas.PvPSafeToMoveOut),
      new And(
        new UnitsAtLeast(1, Protoss.Dragoon, complete = true),
        new EnemyStrategy(With.fingerprints.proxyGateway))),
    new Attack,
    new If(
      new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.oneGateCore),
      new PvPIdeas.AttackSafely))

  override def placementPlan: Plan = new Parallel(
    super.placementPlan,
    new If(
      new Not(new EnemyStrategy(With.fingerprints.twoGate)),
      new ProposePlacement {
        override lazy val blueprints = Vector(new Blueprint(this, building = Some(Protoss.Pylon), requireZone = Some(With.geography.ourNatural.zone)))
      }))

  override def blueprints = Vector(
    new Blueprint(this, building = Some(Protoss.Pylon),           placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 10.0)),
    new Blueprint(this, building = Some(Protoss.Gateway),         placement = Some(PlacementProfiles.wallGathering)),
    new Blueprint(this, building = Some(Protoss.Pylon),           placement = Some(PlacementProfiles.wallGathering)),
    new Blueprint(this, building = Some(Protoss.ShieldBattery)),
    new Blueprint(this, building = Some(Protoss.Gateway),         placement = Some(PlacementProfiles.wallGathering)),
    new Blueprint(this, building = Some(Protoss.CyberneticsCore), placement = Some(PlacementProfiles.wallGathering)),
    new Blueprint(this, building = Some(Protoss.Pylon),           placement = Some(PlacementProfiles.backPylon)),
    new Blueprint(this, building = Some(Protoss.Forge),           placement = Some(PlacementProfiles.wallGathering)),
    new Blueprint(this, building = Some(Protoss.Pylon),           placement = Some(PlacementProfiles.wallGathering)))

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

    new Trigger(
      new UnitsAtLeast(1, Protoss.CitadelOfAdun),
      new CapGasAt(300),
      new CapGasAt(200)),

    // Delay build until scout cleared
    new If(
      new Or(
        new ScoutCleared,
        new FrameAtLeast(GameTime(4, 10)())),

      new Parallel(
        new CancelOrders(Protoss.CyberneticsCore),
        new BuildOrder(
          Get(Protoss.CitadelOfAdun),
          Get(Protoss.TemplarArchives),
          Get(2, Protoss.Gateway),
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
        new If(
          new And(
            new EnemyStrategy(With.fingerprints.twoGate),
            new Not(new SafeAtHome),
            new UnitsAtMost(12, UnitMatchWarriors)),
          new Pump(Protoss.Dragoon)),
        new RequireMiningBases(2),
        new If(
          new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)),
          new BuildCannonsAtNatural(2))),

    new BuildOrder(
      Get(1, Protoss.Dragoon),
      Get(Protoss.AirDamage))),

    new Pump(Protoss.Dragoon)
  )
}
