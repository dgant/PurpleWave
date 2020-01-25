package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstRush
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, StartPositionsAtLeast}
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.{PvZ2Gate1012, PvZ4GateGoon}

class PvZ2Gate1012 extends GameplanTemplate {

  override val activationCriteria = new Employing(PvZ2Gate1012)
  override val completionCriteria = new Latch(new MiningBasesAtLeast(2))
  override def buildOrder      = ProtossBuilds.TwoGate1012
  override def workerPlan      = NoPlan()
  override val scoutPlan: Plan = new If(new StartPositionsAtLeast(4), new ScoutOn(Protoss.Pylon), new ScoutOn(Protoss.Gateway))
  override def placementPlan   = new ProposePlacement {
    override lazy val blueprints = Vector(
      new Blueprint(this, building = Some(Protoss.Pylon),   placement = Some(PlacementProfiles.hugTownHall)),
      new Blueprint(this, building = Some(Protoss.Gateway), placement = Some(PlacementProfiles.hugTownHall)),
      new Blueprint(this, building = Some(Protoss.Gateway), placement = Some(PlacementProfiles.hugTownHall)),
      new Blueprint(this, building = Some(Protoss.Pylon),   placement = Some(PlacementProfiles.backPylon)),
      new Blueprint(this, building = Some(Protoss.Pylon),   placement = Some(PlacementProfiles.hugTownHall)),
      new Blueprint(this, building = Some(Protoss.Pylon),   placement = Some(PlacementProfiles.hugTownHall)))
}
  override def aggressionPlan = new If(
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
      new Or(
        new UpgradeComplete(Protoss.GroundDamage),
        new UnitsAtLeast(6, Protoss.Dragoon, complete = true)),
      new Attack,
      new If(
        new EnemyStrategy(With.fingerprints.twelveHatch, With.fingerprints.tenHatch, With.fingerprints.overpool),
        new Trigger(new UnitsAtLeast(2, UnitMatchWarriors, complete = true), super.attackPlan),
        new Trigger(new UnitsAtLeast(4, UnitMatchWarriors, complete = true), super.attackPlan)))

  class RespectMutalisks extends Or(
    new EnemyHasShown(Zerg.Lair),
    new EnemyHasShown(Zerg.Spire),
    new EnemyHasShown(Zerg.Mutalisk))

  class GettingGoons extends Or(
    new RespectMutalisks,
    new Employing(PvZ4GateGoon),
    new UnitsAtLeast(12, Protoss.Zealot, complete = true))

  class GettingGoonsASAP extends Or(
    new RespectMutalisks,
    new And(
      new Employing(PvZ4GateGoon),
      new UnitsAtLeast(8, Protoss.Zealot)))

  override def buildPlans = Vector(

    new DefendFightersAgainstRush,

    // Emergency detection
    // or limit gas
    new If(
      new EnemyHasShownCloakedThreat,
      new Build(
        Get(Protoss.Assimilator),
        Get(Protoss.CyberneticsCore),
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Observatory),
        Get(2, Protoss.Observer)),
      new If(
        new GettingGoons,
        new CapGasWorkersAtRatio(.14),
        new If(
          new GasForUpgrade(Protoss.GroundDamage),
          new CapGasAt(0),
          new Parallel(new CapGasAt(100))))),

    // Emergency Dragoons
    new If(
      new GettingGoonsASAP,
      new Build(
        Get(Protoss.Assimilator),
        Get(Protoss.CyberneticsCore),
        Get(Protoss.DragoonRange))),

    new If(
      new Or(
        new MineralsAtLeast(700),

        new And(
          // Is it safe?
          new And(
            new SafeAtHome,
            new EnemiesAtLeast(4, Zerg.SunkenColony, complete = true)),

          // Will we survive an influx of attackers?
          new Or(
            new UnitsAtLeast(12, Protoss.Dragoon, complete = true),
            new And(
              new Not(new RespectMutalisks),
              new UnitsAtLeast(12, UnitMatchWarriors, complete = true))))),
      new RequireMiningBases(2)),

    // Train army/workers
    new Pump(Protoss.Probe, 16),
    new PumpRatio(Protoss.Dragoon, 0, 24, Seq(Enemy(Zerg.Mutalisk, 1.0), Enemy(Zerg.Lurker, 1.0))),
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
          new If(
            new UpgradeStarted(Protoss.DragoonRange),
            new Pump(Protoss.Dragoon)),
            new Pump(Protoss.Zealot, 7)),

        // Workers?
        new PumpWorkers),

      // Post-assimilator
      new Parallel(
        new PumpWorkers,
        new Build(Get(Protoss.Assimilator)),
        new If(
          new GettingGoons,
          new Build(
            Get(Protoss.CyberneticsCore),
            Get(Protoss.DragoonRange)),
          new Parallel(
            // They will be stuck making Zerglings with no third Hatchery, so we can punish with mass slowlots
            new If(new EnemyStrategy(With.fingerprints.tenHatch, With.fingerprints.twelvePool), new Build(Get(3, Protoss.Gateway))),
            new Build(
              Get(Protoss.Forge),
              Get(Protoss.GroundDamage)))),
        new Build(Get(4, Protoss.Gateway)))),

    new Pump(Protoss.Zealot),

    new If(
      new UnitsAtLeast(4, Protoss.Gateway, complete = true),
      new RequireMiningBases(2))
  )
}

