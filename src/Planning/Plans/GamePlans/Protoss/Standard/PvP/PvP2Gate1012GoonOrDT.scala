package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.{Aggression, Attack, EjectScout}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.{CapGasAt, CapGasWorkersAt, Pump}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Placement.BuildCannonsAtNatural
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, EnemyBasesAtMost, SafeAtHome}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, StartPositionsAtLeast}
import Planning.UnitMatchers.{UnitMatchOr, UnitMatchWarriors}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvP2Gate1012DT, PvP2Gate1012Goon}

class PvP2Gate1012GoonOrDT extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvP2Gate1012Goon, PvP2Gate1012DT)
  override val completionCriteria: Predicate = new Latch(new BasesAtLeast(2))

  override def blueprints = Vector(
    new Blueprint(Protoss.Pylon,         placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 8.0)),
    new Blueprint(Protoss.Gateway,       placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 4.0)),
    new Blueprint(Protoss.Gateway,       placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 4.0)),
    new Blueprint(Protoss.Pylon,         placement = Some(PlacementProfiles.backPylon)),
    new Blueprint(Protoss.ShieldBattery),
    new Blueprint(Protoss.Gateway,       placement = Some(PlacementProfiles.defensive)),
    new Blueprint(Protoss.Pylon),
    new Blueprint(Protoss.Pylon),
    new Blueprint(Protoss.Pylon),
    new Blueprint(Protoss.Pylon),
    new Blueprint(Protoss.Pylon, requireZone = Some(With.geography.ourNatural.zone)))

  override def aggressionPlan: Plan = new If(
    new Or(
      new UpgradeComplete(Protoss.ZealotSpeed),
      new And(
        new Not(new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway)),
        new Not(new EnemyHasUpgrade(Protoss.DragoonRange)))),
    new Aggression(1.25),
    super.aggressionPlan)

  override def attackPlan: Plan = new If(
    new Or(
      new Not(new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway)),
      new EnemyHasShown(Protoss.Assimilator),
      new EnemyHasShown(Protoss.CyberneticsCore),
      new EnemyHasShown(Protoss.Dragoon),
      new EnemyHasShown(Protoss.CitadelOfAdun),
      new EnemyHasShown(Protoss.TemplarArchives),
      new EnemyBasesAtLeast(2),
      new UnitsAtLeast(3, Protoss.Dragoon, complete = true)),
    new If(
      new And(
        new UnitsAtLeast(2, Protoss.DarkTemplar, complete = true),
        new EnemiesAtMost(0, Protoss.Observer)),
      new Attack,
      new If(
        new Not(
          new And(
            new MiningBasesAtLeast(2),
            new EnemyStrategy(With.fingerprints.fourGateGoon))),
        new PvPIdeas.AttackSafely)))

  override val initialScoutPlan: Plan = new If(new StartPositionsAtLeast(4), new ScoutOn(Protoss.Pylon), new ScoutOn(Protoss.Gateway))
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush)

  class GoDT extends And(
    new EnemiesAtMost(0, Protoss.PhotonCannon),
    new EnemiesAtMost(0, Protoss.Forge),
    new Or(
      new Employing(PvP2Gate1012DT),
      new EnemyStrategy(With.fingerprints.fourGateGoon, With.fingerprints.proxyGateway)),
    new Or(
      new UnitsAtLeast(1, Protoss.CitadelOfAdun),
      new Not(new EnemyStrategy(With.fingerprints.robo))))

  class NeedForgeToExpand extends Or(
    new EnemyStrategy(With.fingerprints.dtRush, With.fingerprints.forgeFe), // Forge FE can hide a DT rush
    new And(
      new Not(new EnemyStrategy(With.fingerprints.robo)),
      new EnemyBasesAtMost(1)))

  class Expand extends Parallel(
    new If(new NeedForgeToExpand, new BuildCannonsAtNatural(0)), // Build the Forge and a Pylon
    new RequireMiningBases(2),
    new If(new NeedForgeToExpand, new BuildCannonsAtNatural(1)))

  override val buildOrder: Vector[BuildRequest] = ProtossBuilds.TwoGate1012
  override def buildPlans = Vector(

    new CapGasAt(300),
    new If(
      new And(
        new UnitsAtMost(2, Protoss.Gateway),
        new Not(new GoDT)),
      new CapGasWorkersAt(2)),

    new If(new UnitsAtLeast(1, Protoss.Dragoon, complete = true), new EjectScout),

    new If(new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.proxyGateway, With.fingerprints.twoGate), new BuildOrder(Get(7, Protoss.Zealot))),

    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)),

    new If(
      new And(
        new GoDT,
        new UnitsAtLeast(1, Protoss.CyberneticsCore)),
      new EjectScout(Protoss.Probe)),

    new If(
      new EnemyStrategy(With.fingerprints.forgeFe),
      new BuildOrder(
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Shuttle),
        Get(Protoss.RoboticsSupportBay))),

    new If(new Not(new GoDT), new Build(Get(Protoss.DragoonRange))),

    new If(
      new Or(
        new UnitsAtLeast(2, UnitMatchOr(Protoss.DarkTemplar, Protoss.Reaver), complete = true),
        new And(new SafeAtHome, new UnitsAtLeast(12, UnitMatchWarriors))),
      new Expand),

    new If(
      new GoDT,
      new Parallel(
        new BuildOrder(
          Get(Protoss.Dragoon),
          Get(Protoss.CitadelOfAdun),
          Get(Protoss.TemplarArchives)),
        new FlipIf(
          new GasAtLeast(260), // If we get Leg Enhancements with less gas than this, our Dark Templar will be delayed
          new Build(Get(2, Protoss.DarkTemplar)),
          new Build(Get(Protoss.ZealotSpeed))))),

    new If(
      new And(
        new GoDT,
        new Not(new UpgradeStarted(Protoss.DragoonRange))),
      new Parallel(
        new Pump(Protoss.DarkTemplar, 4),
        new Pump(Protoss.Zealot),
        new Trigger(
          new UnitsAtLeast(2, Protoss.DarkTemplar),
          new Build(Get(Protoss.DragoonRange))))),

    new FlipIf(
      new SafeAtHome,
      new PvPIdeas.TrainArmy,
      new Build(Get(3, Protoss.Gateway))),

    new FlipIf(
      new SafeAtHome,
      new Expand,
      new Build(Get(4, Protoss.Gateway))),

    new If(new NeedForgeToExpand, new BuildCannonsAtNatural(2)),
  )
}
