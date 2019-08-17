package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Placement.{BuildCannonsAtNatural, BuildCannonsInMain, ProposePlacement}
import Planning.Plans.Scouting.{ScoutForCannonRush, ScoutOn}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyDarkTemplarLikely, SafeAtHome}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP2GateDTExpand

class PvP2GateDarkTemplar extends GameplanTemplate {
  
  override val activationCriteria = new Employing(PvP2GateDTExpand)
  override val completionCriteria = new Latch(new MiningBasesAtLeast(2))
  override val workerPlan = NoPlan()
  override val scoutPlan = new ScoutOn(Protoss.Gateway)

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
        override lazy val blueprints = Vector(new Blueprint(Protoss.Pylon, requireZone = Some(With.geography.ourNatural.zone)))
      }
    )
  )
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
    new ScoutForCannonRush
  )
  
  override val buildPlans = Vector(
    new EjectScout,
    new RequireSufficientSupply,
    new Trigger(
      new UnitsAtLeast(1, Protoss.CitadelOfAdun),
      initialBefore = new CapGasWorkersAt(2)),
    new If(
      new And(
        new EnemiesAtMost(0, Protoss.Observer),
        new EnemiesAtMost(0, Protoss.Observatory),
        new UnitsAtMost(2, Protoss.DarkTemplar)),
        new Pump(Protoss.DarkTemplar, 3, 1)),
    new CapGasAt(300),
    new If(
      new EnemyDarkTemplarLikely,
      new Parallel(
        new BuildCannonsInMain(1),
        new BuildCannonsAtNatural(2),
        new BuildCannonsInMain(2),
        new BuildCannonsAtNatural(3))),
    new BuildCannonsAtNatural(0),
    new If(new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)), new BuildCannonsAtNatural(1)),
    new If(
      new And(
        new EnemyStrategy(With.fingerprints.twoGate),
        new Not(new SafeAtHome),
        new UnitsAtMost(12, UnitMatchWarriors)),
      new Pump(Protoss.Dragoon)),
    new RequireMiningBases(2),
    new PumpWorkers,
    new If(new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)), new BuildCannonsAtNatural(2)),
    new Pump(Protoss.Dragoon),
    // In case we're struggling to get out of our base
    new If(
      new MineralsAtLeast(400),
      new Build(Get(5, Protoss.Gateway))),
    new Trigger(
      new UnitsAtLeast(2, Protoss.DarkTemplar),
      new Build(Get(Protoss.DragoonRange)))
  )
}
