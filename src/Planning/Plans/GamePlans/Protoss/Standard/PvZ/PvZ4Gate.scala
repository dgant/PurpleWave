package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.Aggression
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstEarlyPool
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Placement.ProposePlacement
import Planning.Plans.Scouting.{ScoutExpansionsAt, ScoutOn}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, StartPositionsAtLeast}
import Planning.UnitMatchers.{UnitMatchWarriors, UnitMatchWorkers}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.PvZ4Gate1012

class PvZ4Gate extends GameplanTemplate {
  
  override val activationCriteria = new Employing(PvZ4Gate1012)
  override val completionCriteria = new Latch(new MiningBasesAtLeast(2))
  override def buildOrder      = ProtossBuilds.TwoGate1012
  override def workerPlan      = NoPlan()
  override val scoutPlan: Plan = new If(new StartPositionsAtLeast(4), new ScoutOn(Protoss.Pylon), new ScoutOn(Protoss.Gateway))
  override def scoutExposPlan  = new ScoutExpansionsAt(55)
  override def placementPlan   = new ProposePlacement {
    override lazy val blueprints = Vector(
      new Blueprint(Protoss.Pylon,   placement = Some(PlacementProfiles.hugTownHall)),
      new Blueprint(Protoss.Gateway, placement = Some(PlacementProfiles.hugTownHall)),
      new Blueprint(Protoss.Gateway, placement = Some(PlacementProfiles.hugTownHall)),
      new Blueprint(Protoss.Pylon,   placement = Some(PlacementProfiles.backPylon)),
      new Blueprint(Protoss.Pylon,   placement = Some(PlacementProfiles.hugTownHall)),
      new Blueprint(Protoss.Pylon,   placement = Some(PlacementProfiles.hugTownHall)))
}
  override def aggressionPlan  = new If(
    new UnitsAtMost(8, UnitMatchWarriors, complete = true),
    new Aggression(1.0),
    new If(
      new UnitsAtMost(10, UnitMatchWarriors, complete = true),
      new Aggression(1.2),
      new If(
        new UnitsAtMost(15, UnitMatchWarriors, complete = true),
        new Aggression(1.4),
        new Aggression(2.0))))
  
  override def attackPlan: Plan =
    new If(
      new EnemyStrategy(With.fingerprints.twelveHatch, With.fingerprints.tenHatch, With.fingerprints.overpool),
      new Trigger(new UnitsAtLeast(2, UnitMatchWarriors, complete = true), super.attackPlan),
      new Trigger(new UnitsAtLeast(8, UnitMatchWarriors, complete = true), super.attackPlan))

  class RespectMutalisks extends Or(
    new EnemyHasShown(Zerg.Lair),
    new EnemyHasShown(Zerg.Spire),
    new EnemyHasShown(Zerg.Mutalisk))

  override def buildPlans = Vector(

    new DefendFightersAgainstEarlyPool,

    new If(
      new EnemyHasShownCloakedThreat,
      new Build(
        Get(Protoss.Assimilator),
        Get(Protoss.CyberneticsCore),
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Observatory),
        Get(2, Protoss.Observer)),
      new If(
        new UnitsAtMost(14, UnitMatchWorkers),
        new CapGasAt(0),
        new If(
          new Not(new RespectMutalisks),
          new If(
            new UpgradeStarted(Protoss.DragoonRange),
            new CapGasAtRatioToMinerals(0.4),
            new CapGasAt(150))))),

    new If(
      new Or(
      new RespectMutalisks,
        new UnitsAtLeast(8, Protoss.Zealot)),
      new Build(
        Get(Protoss.Assimilator),
        Get(Protoss.CyberneticsCore),
        Get(Protoss.DragoonRange))),

    new Trigger(
      new Or(
        new MineralsAtLeast(800),
        new UnitsAtLeast(24, UnitMatchWarriors),
        new And(
          new UnitsAtLeast(6, Protoss.Dragoon),
          new EnemiesAtLeast(3, Zerg.SunkenColony, complete = true))),
      new RequireMiningBases(2)),

    new PumpRatio(Protoss.Dragoon, 0, 20, Seq(Enemy(Zerg.Mutalisk, 1.0), Enemy(Zerg.Lurker, 1.0))),
    new FlipIf(
      new And(
        new UnitsAtLeast(1, Protoss.Assimilator, complete = true),
        new SafeAtHome),

      // Pre-assimilator
      new FlipIf(
        new Or(
        new UnitsAtLeast(6, UnitMatchWarriors),
          new And(
            new UnitsAtLeast(3, UnitMatchWarriors),
            new SafeAtHome)),

        // Army?
        new Parallel(
          new PumpRatio(Protoss.Zealot, 0, 20, Seq(Enemy(Zerg.Zergling, 0.3))),
          new If(new UpgradeStarted(Protoss.DragoonRange), new Pump(Protoss.Dragoon)),
          new Pump(Protoss.Zealot)),

        // Workers?
        new PumpWorkers),

      // Post-assimilator
      new Parallel(
        new PumpWorkers,
      new Build(
        Get(Protoss.Assimilator),
        Get(Protoss.CyberneticsCore),
        Get(4, Protoss.Gateway),
          Get(Protoss.DragoonRange)),
        new Pump(Protoss.Dragoon),
        new Pump(Protoss.Zealot))),

    new If(
      new UnitsAtLeast(4, Protoss.Gateway, complete = true),
      new RequireMiningBases(2))
  )
}

