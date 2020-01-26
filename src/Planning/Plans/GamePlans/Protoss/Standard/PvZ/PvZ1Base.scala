package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Attack, ConsiderAttacking, Hunt}
import Planning.Plans.Basic.{NoPlan, WriteStatus}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstRush
import Planning.Plans.GamePlans.Protoss.Standard.PvZ.PvZIdeas.MeldArchonsUntilStorm
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.{Chill, ScoutOn}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, StartPositionsAtLeast}
import Planning.UnitMatchers.{UnitMatchAntiAir, UnitMatchWarriors}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.{PvZ4GateGoon, PvZCorsair, PvZDT, PvZSpeedlot}

class PvZ1Base extends GameplanTemplate {

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
      new Blueprint(this, building = Some(Protoss.Pylon),   placement = Some(PlacementProfiles.hugTownHall))) }

  override def priorityAttackPlan: Plan = new Attack(Protoss.DarkTemplar)
  override def attackPlan: Plan = new Parallel(
    new Chill(Protoss.HighTemplar),
    new Hunt(Protoss.Corsair, Zerg.Overlord),
    new If(
      new And(new EnemiesAtMost(0, Zerg.Mutalisk), new EnemiesAtMost(0, Zerg.Scourge)),
      new Attack(Protoss.Corsair)),
    new If(
      new Or(
        new UpgradeComplete(Protoss.GroundDamage),
        new UnitsAtLeast(6, Protoss.Dragoon, complete = true)),
      new Attack,
      new If(
        new EnemyStrategy(With.fingerprints.twelveHatch, With.fingerprints.tenHatch, With.fingerprints.overpool),
        new Trigger(new UnitsAtLeast(2, UnitMatchWarriors, complete = true), new ConsiderAttacking),
        new Trigger(new UnitsAtLeast(4, UnitMatchWarriors, complete = true), new ConsiderAttacking))))

  class EnemyHydralisks extends Or(
    new EnemyHasShown(Zerg.Hydralisk),
    new EnemiesAtLeast(1, Zerg.Hydralisk))

  class GettingAntiAirASAP extends Or(
    new EnemyHasShown(Zerg.Lair),
    new EnemyHasShown(Zerg.Spire),
    new EnemyHasShown(Zerg.Mutalisk))

  class GettingArchons extends And(
    new GettingAntiAirASAP,
    new UnitsAtLeast(1, Protoss.TemplarArchives))

  class GettingGoons extends Or(
    new Employing(PvZ4GateGoon),
    new And(
      new Employing(PvZCorsair),
      new Or(
        new EnemyHydralisks,
        new Latch(new UnitsAtLeast(2, Protoss.Corsair)))),
    new And(
      new GettingAntiAirASAP,
      new Not(new GettingArchons)))

  class GettingZealots extends And(
    new Employing(PvZSpeedlot),
    new Not(new GettingGoons))

  class GettingCorsair extends And(
    new Employing(PvZCorsair),
    new Not(new EnemyHydralisks))

  class GettingDT extends And(
    new Employing(PvZDT),
    new Not(new GettingArchons))

  override def archonPlan: Plan = new MeldArchonsUntilStorm

  override def buildPlans = Vector(

    new DefendFightersAgainstRush,

    new If(new GettingAntiAirASAP,      new WriteStatus("Anti-AirASAP")),
    new If(new GettingGoons,            new WriteStatus("4-Gate Goons")),
    new If(new GettingZealots,          new WriteStatus("+1 Speedlot")),
    new If(new GettingCorsair,          new WriteStatus("Corsair")),
    new If(new GettingDT,               new WriteStatus("DT Expand")),

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
        // Definitely don't cap gas for these
        new And(
          new Not(new GettingArchons),
          new Not(new GettingDT),
          new Not(new GettingCorsair)),
        new If(
          new GettingGoons,
          new CapGasWorkersAtRatio(.14),
          new If(
            new GasForUpgrade(Protoss.ZealotSpeed),
            new CapGasAt(0),
            new CapGasAt(250))))),

    // Emergency Dragoons
    new If(
      new GettingAntiAirASAP,
      new Parallel(
        new Build(Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore)),
        new If(new GettingArchons, new Build(Get(Protoss.CitadelOfAdun), Get(Protoss.TemplarArchives))),
        new If(new GettingGoons, new Build(Get(Protoss.DragoonRange))))),

    // Expand
    new If(
      new Or(
        new MineralsAtLeast(700),
        new UnitsAtLeast(2, Protoss.DarkTemplar, complete = true),
        new UnitsAtLeast(2, Protoss.Archon, complete = true),
        new And(
          new Or(
            new Not(new Or(new GettingArchons, new GettingDT)),
            new UnitsAtLeast(1, Protoss.TemplarArchives)),
          new Or(
            new Not(new GettingAntiAirASAP),
            new UnitsAtLeast(8, UnitMatchAntiAir, complete = true)),
          new Or(
            new And(new UnitsAtLeast(6,   UnitMatchWarriors), new EnemiesAtLeast(4, Zerg.SunkenColony, complete = true)),
            new And(new UnitsAtLeast(8,   UnitMatchWarriors), new EnemiesAtLeast(3, Zerg.SunkenColony, complete = true)),
            new And(new UnitsAtLeast(12,  UnitMatchWarriors), new EnemiesAtLeast(2, Zerg.SunkenColony, complete = true)),
            new And(new UnitsAtLeast(14,  UnitMatchWarriors), new SafeAtHome),
            new UnitsAtLeast(20, UnitMatchWarriors, complete = true)))),
      new RequireMiningBases(2)),

    // Train army/workers
    new Pump(Protoss.Probe, 16),
    new If(new GettingArchons, new Pump(Protoss.HighTemplar)),
    new If(new GettingDT, new Pump(Protoss.DarkTemplar, 4)),
    new If(new GettingGoons, new Parallel(new UpgradeContinuously(Protoss.DragoonRange), new Pump(Protoss.Dragoon))),
    new Pump(Protoss.DarkTemplar, 1),
    new If(new GettingCorsair, new Pump(Protoss.Corsair, 1)),
    new Pump(Protoss.HighTemplar),
    new Pump(Protoss.Dragoon, 1), // For ejecting Overlords and such
    new Pump(Protoss.Zealot, 7), // Save minerals for remaining tech + gateways
    new PumpWorkers,
    new If(new GettingCorsair, new Pump(Protoss.Corsair, 6)),

    new Build(Get(2, Protoss.Pylon), Get(Protoss.Assimilator)),
    new If(new GettingCorsair,  new Build(Get(Protoss.CyberneticsCore), Get(Protoss.Stargate))),
    new If(new GettingArchons,  new Build(Get(Protoss.CyberneticsCore), Get(Protoss.CitadelOfAdun), Get(Protoss.TemplarArchives), Get(5, Protoss.Gateway))),
    new If(new GettingDT,       new Build(Get(Protoss.CyberneticsCore), Get(Protoss.CitadelOfAdun), Get(Protoss.TemplarArchives))),
    new If(new GettingZealots,  new Build(Get(Protoss.CyberneticsCore), Get(Protoss.Forge),         Get(Protoss.GroundDamage),    Get(2, Protoss.Gateway), Get(Protoss.CitadelOfAdun), Get(Protoss.ZealotSpeed), Get(Protoss.TemplarArchives), Get(5, Protoss.Gateway))),
    new If(new GettingGoons,    new Build(Get(Protoss.CyberneticsCore), Get(4, Protoss.Gateway))),

    new Pump(Protoss.Zealot),
    new Build(Get(4, Protoss.Gateway)),
    new If(new UnitsAtLeast(4, Protoss.Gateway, complete = true), new RequireMiningBases(2))
  )
}

