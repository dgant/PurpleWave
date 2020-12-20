package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Attack, Hunt}
import Planning.Plans.Basic.WriteStatus
import Planning.Plans.Compound.{If, Trigger, _}
import Planning.Plans.GamePlans.Protoss.Situational.{BuildHuggingNexus, DefendFightersAgainstRush}
import Planning.Plans.Macro.Automatic.{PumpWorkers, _}
import Planning.Plans.Macro.Build.CancelIncomplete
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.MeldDarkArchons
import Planning.Predicates.Compound.{And, Check, Latch, Not}
import Planning.Predicates.Economy.{GasAtLeast, GasAtMost, MineralsAtLeast}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, SafeToMoveOut}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers._
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss._
import Utilities.{GameTime, Minutes}

object PvTIdeas {

  class PriorityAttacks extends Parallel(
    new Hunt(Protoss.DarkArchon, Terran.Ghost),
    new Hunt(Protoss.DarkArchon, Terran.ScienceVessel),
    new If(new Or(new EnemyUnitsNone(Protoss.Observer), new EnemyBasesAtLeast(3)), new Attack(Protoss.DarkTemplar)),
    new Attack(Protoss.Scout),
    new Trigger(new UnitsAtLeast(4, Protoss.Carrier, complete = true), initialAfter = new Attack(Protoss.Carrier)))

  class PvTAttack extends Trigger(
    new UnitsAtLeast(4, Protoss.Carrier, complete = true),
    new Attack,
    new Attack(UnitMatchAnd(UnitMatchRecruitableForCombat, UnitMatchNot(UnitMatchWorkers), UnitMatchNot(Protoss.Carrier))))

  class EnemyBarracksCheese extends And(
    new EnemyStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs, With.fingerprints.twoRax1113),
    new Not(new EnemyHasShown(Terran.Vulture)),
    new Not(new EnemyHasShown(Terran.SiegeTankUnsieged)),
    new Not(new EnemyHasShown(Terran.SiegeTankSieged)))

  class NeedToPressureBarracksCheese extends And(new EnemyBarracksCheese, new Check(() => With.strategy.isFlat || With.strategy.isInverted))

  class AttackSafely extends If(
    new NeedToPressureBarracksCheese,
    new Attack,
    new If(
      new Or(
        new Latch(new UnitsAtLeast(8, UnitMatchWarriors)),
        new Not(new EnemyBarracksCheese)),
      new If(
        new And(
          // It's safe -- or necessary for our build -- to attack in general
          new Or(
            new SafeToMoveOut,
            new And(
              new FrameAtMost(GameTime(4, 0)()),
              new Employing(PvT32Nexus, PvT1015Expand, PvT1015DT, PvTStove))),
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
              new Latch(new UnitsAtLeast(24, UnitMatchWarriors)),
              // Vs 2-fac: Turtle medium
              new And(
                new Latch(new UnitsAtLeast(12, UnitMatchWarriors)),
                new Not(new EnemyStrategy(With.fingerprints.threeFac))),
              new Not(new EnemyStrategy(With.fingerprints.twoFac, With.fingerprints.threeFac))))),
          new PvTAttack)))

  class ReactToFiveRaxAs2GateCore extends Parallel(
    new If(
      new And(
        new EnemyStrategy(With.fingerprints.fiveRax),
        new FramesUntilUnitAtLeast(Protoss.CyberneticsCore, Protoss.Zealot.buildFrames / 3)),
      new BuildOrder(Get(2, Protoss.Zealot))),
    new Parallel(
      new WriteStatus("ReactToFiveRaxAs2GateCore"),
      new If(
        new EnemyStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs, With.fingerprints.twoRax1113),
        new DefendFightersAgainstRush)))

  class ReactToWorkerRush extends If(
    new And(
      new EnemyStrategy(With.fingerprints.workerRush),
      new EnemiesAtMost(0, UnitMatchWarriors),
      new BasesAtMost(2),
      new FrameAtMost(GameTime(8, 0)())),
    new Parallel(
      new WriteStatus("ReactToWorkerRush"),
      new Pump(Protoss.Probe, 9),
      new If(
        new UnitsAtMost(3, UnitMatchWarriors),
        new CancelIncomplete(Protoss.Nexus)),
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
      new BuildHuggingNexus,
      new Build(Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore), Get(2, Protoss.Pylon), Get(2, Protoss.Gateway), Get(Protoss.DragoonRange))))

  class ReactToBunkerRush extends If(
    new And(
      new EnemyStrategy(With.fingerprints.bunkerRush),
      new FrameAtMost(Minutes(7)()),
      new Or(
        new EnemiesAtLeast(1, UnitMatchAnd(Terran.Bunker, UnitMatchProxied)),
        new EnemiesAtLeast(1, Terran.Marine))),
    new Parallel(
      new WriteStatus("ReactToBunkerRush"),
      new If(
        new UnitsAtLeast(1, Protoss.CyberneticsCore, complete = true),
        new Pump(Protoss.Dragoon, 3),
        new Pump(Protoss.Zealot, 3))))

  class ReactToBBS extends If(
    new And(
      new FrameAtMost(GameTime(10, 0)()),
      new EnemyStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs, With.fingerprints.twoRax1113)),
    new Parallel(
      new WriteStatus("ReactToBBS"),
      new DefendFightersAgainstRush,
      new CapGasAt(250),
      new If(new UnitsAtMost(1, Protoss.Gateway, complete = true), new CapGasWorkersAt(1)),
      new If(new UnitsAtMost(5, UnitMatchWarriors), new CancelIncomplete(Protoss.Nexus, Protoss.CitadelOfAdun, Protoss.TemplarArchives)),
      new If(new UnitsAtMost(1, Protoss.Gateway), new CancelIncomplete(UnitMatchOr(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Nexus, Protoss.Stargate))),
      new RequireSufficientSupply,
      new If(new UnitsAtLeast(1, Protoss.Reaver, complete = true), new RequireMiningBases(2)),
      new Pump(Protoss.Reaver),
      new If(new UnitsAtLeast(2, Protoss.Gateway), new PumpWorkers, new PumpWorkers(cap = 12)),
      new Build(Get(Protoss.DragoonRange)),
      new Pump(Protoss.Dragoon),
      new Pump(Protoss.Zealot, 7),
      new Build(
        Get(Protoss.Pylon),
        Get(2, Protoss.Gateway),
        Get(18, Protoss.Probe)),
      new If(new BasesAtMost(1), new Build(Get(Protoss.ShieldBattery))),
      new Pump(Protoss.Probe),
      new Build(
        Get(2, Protoss.Gateway),
        Get(Protoss.Assimilator),
        Get(Protoss.CyberneticsCore),
        Get(Protoss.DragoonRange),
        Get(Protoss.RoboticsFacility),
        Get(Protoss.RoboticsSupportBay),
        Get(2, Protoss.Nexus))))

  class TrainMinimumDragoons extends Parallel(
    new PumpRatio(Protoss.Dragoon, 1, 5, Seq(Enemy(Terran.Vulture, 1.0), Enemy(Terran.Wraith, 1.0))),
    new PumpRatio(Protoss.Dragoon, 1, 20, Seq(Enemy(Terran.Vulture, 0.75), Enemy(Terran.Wraith, 0.5))))

  class EnemyHasMines extends Or(new EnemyHasShown(Terran.SpiderMine), new EnemyHasTech(Terran.SpiderMinePlant))

  class TrainDarkTemplar extends If(
    new And(
      // Can't spare gas on top of Carriers
      new UnitsAtMost(0, Protoss.FleetBeacon),
      // Use DTs to drain ComSet energy prior to Arbiters,
      // but there's no point in having both at the same time
      new UnitsAtMost(0, Protoss.ArbiterTribunal, complete = true),
      new UnitsAtMost(0, Protoss.Arbiter),
      new EnemiesAtMost(0, Terran.ScienceVessel)),
    new Pump(Protoss.DarkTemplar, 2))

  private class TrainObservers extends If(
    new UnitsAtLeast(24, UnitMatchWarriors),
    new Pump(Protoss.Observer, 4),
    new If(
      new UnitsAtLeast(18, UnitMatchWarriors),
      new Pump(Protoss.Observer, 3),
      new Pump(Protoss.Observer, 2)))

  class TrainReavers extends Parallel(
    new PumpRatio(Protoss.Reaver, 2, 6, Seq(
      Enemy(Terran.Marine, 1.0/6.0),
      Enemy(Terran.Goliath, 1.0/6.0),
      Enemy(Terran.Vulture, 1.0/8.0))))

  class TrainScouts extends If(
    new And(
      new EnemiesAtMost(0, Terran.Goliath),
      new EnemiesAtMost(6, Terran.Marine),
      new EnemiesAtMost(8, Terran.MissileTurret),
      new UnitsExactly(0, Protoss.FleetBeacon),
      new UnitsExactly(0, Protoss.ArbiterTribunal),
      new Employing(PvTStove)),
    new Pump(Protoss.Scout, 5))

  class TrainGatewayUnits extends Parallel(
    new PumpRatio(Protoss.Dragoon, 0, 100, Seq(Enemy(Terran.Wraith, 2.0), Enemy(Terran.Battlecruiser, 5.0))),
    new PumpRatio(Protoss.Dragoon, 8, 24, Seq(Flat(4.0), Enemy(Terran.Vulture, .75))),
    new If(
      new EnemyStrategy(With.fingerprints.bio),
      new Parallel(
        new Pump(Protoss.HighTemplar),
        new Pump(Protoss.Zealot)),
      new Parallel(
        new If(
          new Or(new UpgradeStarted(Protoss.ZealotSpeed), new And(new MineralsAtLeast(600), new GasAtMost(200))),
          new PumpRatio(Protoss.Zealot, 0, 50, Seq(
            Enemy(UnitMatchSiegeTank, 2.5),
            Enemy(Terran.Goliath,     1.5),
            Enemy(Terran.Marine,      1.0),
            Enemy(Terran.Vulture,     -1.25)))),
        new If(new GasAtLeast(800), new Pump(Protoss.HighTemplar, 6, maximumConcurrently = 2)),
        new If(new Employing(PvEStormYes), new PumpRatio(Protoss.HighTemplar, 0, 4, Seq(Flat(-2.0), Friendly(UnitMatchWarriors, 0.1)))),
        new Pump(Protoss.Dragoon))))

  class TrainCarriers extends If(
    new Check(() => With.units.countEnemy(Terran.Goliath) < 8 + 3 * With.units.countOurs(Protoss.Carrier)),
    new Pump(Protoss.Carrier))

  class TrainArmy extends Parallel(
    new TrainDarkTemplar,
    new PumpRatio(Protoss.Shuttle, 0, 1, Seq(Friendly(Protoss.Reaver, 1.0))),
    new PumpRatio(Protoss.Shuttle, 0, 2, Seq(Friendly(Protoss.Reaver, 0.5))),
    new TrainReavers,
    new TrainObservers,
    new TrainMinimumDragoons,
    new If(new And(new Employing(PvEStormYes), new EnemyStrategy(With.fingerprints.bio), new UnitsAtLeast(5, Protoss.Gateway)), new PumpRatio(Protoss.HighTemplar, 1, 5, Seq(Enemy(Terran.Marine, 1.0/5.0)))),
    new If(new And(new Employing(PvEStormYes), new UnitsAtLeast(8, Protoss.Carrier)), new Pump(Protoss.HighTemplar, 2)),
    new If(
      new EnemyHasTech(Terran.Lockdown),
      new Parallel(
        new If(
          new UnitsAtMost(2, Protoss.DarkArchon),
          new MeldDarkArchons,
          new Pump(Protoss.DarkTemplar, maximumTotal = 4, maximumConcurrently = 2)))),
    new If(
      new And(new UnitsAtLeast(1, Protoss.FleetBeacon), new EnemyHasShownWraithCloak),
      new PumpRatio(Protoss.Corsair, 0, 8, Seq(Enemy(Terran.Wraith, 2.0)))),
    new TrainCarriers,
    new PumpRatio(Protoss.Arbiter, 2, 8, Seq(Enemy(UnitMatchSiegeTank, 0.5))),
    new TrainScouts,
    new TrainGatewayUnits)
}

