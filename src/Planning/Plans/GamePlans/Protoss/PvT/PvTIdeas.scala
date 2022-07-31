package Planning.Plans.GamePlans.Protoss.PvT

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Basic.WriteStatus
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Macro.Automatic.{PumpWorkers, _}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Cancel
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Compound._
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, SafeToMoveOut}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Utilities.UnitFilters._
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss._
import Utilities.Time.{GameTime, Minutes}

object PvTIdeas {

  class EnemyBarracksCheese extends And(
    new EnemyStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs, With.fingerprints.twoRax1113),
    new Not(new EnemyHasShown(Terran.Vulture)),
    new Not(new EnemyHasShown(Terran.SiegeTankUnsieged)),
    new Not(new EnemyHasShown(Terran.SiegeTankSieged)))

  class NeedToPressureBarracksCheese extends And(new EnemyBarracksCheese, new Check(() => With.strategy.isFlat || With.strategy.isInverted))

  class AttackSafely extends If(
    new NeedToPressureBarracksCheese,
    new AttackAndHarass,
    new If(
      new Or(
        new Latch(new UnitsAtLeast(8, IsWarrior)),
        new Not(new EnemyBarracksCheese)),
      new If(
        new And(
          // It's safe -- or necessary for our build -- to attack in general
          new Or(
            new SafeToMoveOut,
            new And(
              new FrameAtMost(GameTime(4, 0)()),
              new Employing(PvTZZCoreZ, PvT1015Expand, PvT1015DT, PvTStove))),
          // We won't die to Vulture rush
          new Or(
            new MiningBasesAtLeast(3),
            new EnemyBasesAtLeast(2),
            new EnemyStrategy(With.fingerprints.bio),
            new Not(new EnemyHasShown(Terran.Vulture)),
            new Or(
              // Don't get sieged in
              new EnemyHasTech(Terran.SiegeMode),
              // Vs 3-fac: Turtle hard
              new Latch(new UnitsAtLeast(24, IsWarrior)),
              // Vs 2-fac: Turtle medium
              new And(
                new Latch(new UnitsAtLeast(12, IsWarrior)),
                new Not(new EnemyStrategy(With.fingerprints.threeFac))),
              new Not(new EnemyStrategy(With.fingerprints.twoFac, With.fingerprints.threeFac))))),
          new AttackAndHarass)))

  class ReactToFiveRaxAs2GateCore extends If(
    new And(
      new EnemyStrategy(With.fingerprints.fiveRax),
      new FramesUntilUnitAtLeast(Protoss.CyberneticsCore, Protoss.Zealot.buildFrames / 3)),
    new BuildOrder(Get(2, Protoss.Zealot)))

  class ReactToWorkerRush extends If(
    new And(
      new EnemyStrategy(With.fingerprints.workerRush),
      new EnemiesAtMost(0, IsWarrior),
      new BasesAtMost(2),
      new FrameAtMost(GameTime(8, 0)())),
    new Parallel(
      new WriteStatus("ReactToWorkerRush"),
      new Pump(Protoss.Probe, 9),
      new If(
        new UnitsAtMost(3, IsWarrior),
        new Cancel(Protoss.Nexus)),
      new CapGasAt(50),
      new If(
        new UnitsAtLeast(10, Protoss.Probe),
        new CapGasWorkersAt(2),
        new If(
          new UnitsAtLeast(7, Protoss.Probe),
          new CapGasWorkersAt(1),
          new CapGasWorkersAt(0))),
      new Pump(Protoss.Dragoon, 3),
      new Pump(Protoss.Zealot, 3),
      new BuildOrder(
        Get(8, Protoss.Probe),
        Get(Protoss.Pylon),
        Get(10, Protoss.Probe),
        Get(Protoss.Gateway),
        Get(12, Protoss.Probe)),
      new PumpWorkers,
      new Build(Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore), Get(2, Protoss.Pylon), Get(2, Protoss.Gateway), Get(Protoss.DragoonRange))))

  class ReactToBunkerRush extends If(
    new And(
      new EnemyStrategy(With.fingerprints.bunkerRush),
      new FrameAtMost(Minutes(7)()),
      new Or(
        new EnemiesAtLeast(1, IsAll(Terran.Bunker, IsProxied)),
        new EnemiesAtLeast(1, Terran.Marine))),
    new Parallel(
      new WriteStatus("ReactToBunkerRush"),
      new If(
        new UnitsAtLeast(1, Protoss.CyberneticsCore, complete = true),
        new Pump(Protoss.Dragoon, 3),
        new Pump(Protoss.Zealot, 3))))

  class ReactToRaxCheese extends If(
    And(
      FrameAtMost(GameTime(10, 0)()),
      EnemyStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs, With.fingerprints.twoRax1113)),
    new Parallel(
      new WriteStatus("ReactToRaxCheese"),
      new CapGasAt(200),
      new BuildOrder(
        Get(8, Protoss.Probe),
        Get(Protoss.Pylon),
        Get(9, Protoss.Probe),
        Get(Protoss.Gateway),
        Get(10, Protoss.Probe)),
      new RequireSufficientSupply,
      // Worker cut rax cheese requires more extreme measures
      new If(
        EnemyStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs),
        new Parallel(
          new If(UnitsAtMost(1, Protoss.Gateway),
            new Parallel(
              new CapGasWorkersAt(0),
              new Cancel(Protoss.Assimilator, Protoss.CyberneticsCore))))),
      new Pump(Protoss.Reaver),
      new Pump(Protoss.Zealot, 3),
      new PumpWorkers,
      new If(
        UnitsAtLeast(2, Protoss.Reaver),
        new RequireMiningBases(2),
        new Cancel(Protoss.Nexus, Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.Stargate)),
      new Pump(Protoss.Zealot, 7),
      new Build(
        Get(2, Protoss.Gateway),
        Get(Protoss.Assimilator),
        Get(Protoss.CyberneticsCore),
        Get(Protoss.RoboticsFacility),
        Get(Protoss.RoboticsSupportBay),
        Get(Protoss.DragoonRange)),
      new Pump(Protoss.Zealot, 12),
      new Pump(Protoss.Dragoon),
      new Build(
        Get(3, Protoss.Gateway),
        Get(2, Protoss.Nexus))))

  class EnemyHasMines extends Or(EnemyHasShown(Terran.SpiderMine), EnemyHasTech(Terran.SpiderMinePlant))

  class TrainDarkTemplar extends If(
    And(
      // Can't spare gas on top of Carriers
      UnitsAtMost(0, Protoss.FleetBeacon),
      // Use DTs to drain ComSat energy prior to Arbiters,
      // but there's no point in having both at the same time
      UnitsAtMost(0, Protoss.ArbiterTribunal, complete = true),
      UnitsAtMost(0, Protoss.Arbiter),
      EnemiesAtMost(0, Terran.ScienceVessel),
      Not(new EnemyHasMines)),
    new Pump(Protoss.DarkTemplar, 2))

  private class TrainObservers extends If(
    Or(UnitsAtLeast(24, IsWarrior),
      new EnemyHasShownWraithCloak),
    new Pump(Protoss.Observer, 4),
    new If(
      UnitsAtLeast(18, IsWarrior),
      new Pump(Protoss.Observer, 3),
      new Pump(Protoss.Observer, 2)))

  class TrainScouts extends If(
    new And(
      new EnemiesAtMost(0, Terran.Goliath),
      new EnemiesAtMost(6, Terran.Marine),
      new EnemiesAtMost(8, Terran.MissileTurret),
      new UnitsExactly(0, Protoss.FleetBeacon),
      new UnitsExactly(0, Protoss.ArbiterTribunal),
      new Employing(PvTStove)),
    new Pump(Protoss.Scout, 5))

  class PvTPumpReaverShuttle(count: Int) extends If(
    Or(
      Employing(PvT1GateReaver, PvT2BaseReaver),
      EnemyStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs, With.fingerprints.twoRax1113, With.fingerprints.twoRaxAcad, With.fingerprints.bio)),
    new If(
      UnitsAtLeast(1, Protoss.Observatory),
      new PumpShuttleAndReavers(count, shuttleFirst = false),
      new PumpShuttleAndReavers(count)))

  class TrainArmy extends Parallel(
    new TrainDarkTemplar,
    new PvTPumpReaverShuttle(2),
    new PumpRatio(Protoss.Dragoon, 8, 24, Seq(Enemy(Terran.Vulture, .4), Enemy(Terran.Wraith, 0.5), Enemy(Terran.Battlecruiser, 4.0))),
    new TrainObservers,
    new PvTPumpReaverShuttle(6),
    new If(
      And(UnitsAtLeast(12, IsWarrior), Employing(PvEStormYes)),
      new Pump(Protoss.HighTemplar, 2)),
    new If(
      UnitsAtLeast(1, Protoss.FleetBeacon),
      new PumpRatio(Protoss.Corsair, 0, 8, Seq(Enemy(Terran.Wraith, 2.0)))),
    new Pump(Protoss.Carrier, 8),
    new PumpRatio(Protoss.Arbiter, 2, 8, Seq(Enemy(IsTank, 0.5))),
    new TrainScouts,
    new If(
      Or(Employing(PvEStormYes), EnemyStrategy(With.fingerprints.bio), GasAtLeast(800)),
      new Pump(Protoss.HighTemplar, maximumConcurrently = 2)),
    new If(Not(UpgradeStarted(Protoss.ZealotSpeed)), new Pump(Protoss.Dragoon)),
    new Pump(Protoss.Zealot))
}

