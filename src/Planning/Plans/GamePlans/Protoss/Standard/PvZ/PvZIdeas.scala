package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.Protoss.Situational.{DefendFFEWithProbesAgainst4Pool, PlacementForgeFastExpand}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.MeldArchons
import Planning.Predicates.Compound.{And, Check, Latch, Not}
import Planning.Predicates.Milestones.{EnemyHasShownCloakedThreat, _}
import Planning.Predicates.Reactive.{EnemyMutalisks, SafeAtHome, SafeToMoveOut}
import Planning.Predicates.Strategy.{Employing, EnemyRecentStrategy, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss._
import Utilities.GameTime

object PvZIdeas {

  class ConditionalDefendFFEWithProbesAgainst4Pool extends If(
    new And(
      new Latch(new Check(() => With.units.countOurs(Protoss.PhotonCannon) + (With.self.minerals + 24) / 150 >= 2)),
      new Or(
        new EnemyStrategy(With.fingerprints.fourPool),
        new And(
          new Not(new EnemyRecentStrategy(With.fingerprints.twelveHatch, With.fingerprints.twelvePool, With.fingerprints.tenHatch, With.fingerprints.ninePool, With.fingerprints.overpool)),
          new EnemyRecentStrategy(With.fingerprints.fourPool),
          new Check(() => With.scouting.enemyHasScoutedUsWithWorker))),
      new FrameAtMost(GameTime(6, 0)()),
      new UnitsAtLeast(1, Protoss.PhotonCannon, complete = false),
      new UnitsAtMost(3, Protoss.PhotonCannon, complete = true)),
    new DefendFFEWithProbesAgainst4Pool)

  class ConditionalAttack extends If(
    new Or(
      new SafeToMoveOut,
      new BasesAtLeast(3),
      new Employing(PvZProxy2Gate, PvZ2Gate910, PvZ2Gate1012)),
    new Attack)

  class TemplarUpToEight extends MeldArchons(49) { override def maximumTemplar = 8 }

  class MeldArchonsUntilStorm extends If(
    new TechStarted(Protoss.PsionicStorm),
    new TemplarUpToEight,
    new MeldArchons)

  class TakeSafeNatural extends If(
    new Or(
      new And(
        new SafeToMoveOut,
        new UnitsAtLeast(6, UnitMatchWarriors, complete = true)),
      new And(
        new SafeAtHome,
        new UnitsAtLeast(10, UnitMatchWarriors, complete = true)),
      new UnitsAtLeast(14, UnitMatchWarriors, complete = true)),
    new RequireMiningBases(2))

  class PvZRequireMiningBases(bases: Int) extends Parallel(
    new FlipIf(
      new EnemyHasTech(Zerg.Burrow),
      new RequireMiningBases(bases),
      new Build(Get(Protoss.RoboticsFacility), Get(Protoss.Observatory), Get(Protoss.Observer))))
  
  class ReactToLurkers extends Parallel(
    new If(
      new EnemyHasShown(Zerg.Lurker),
      new Parallel(
        new Build(
          Get(Protoss.CyberneticsCore),
          Get(Protoss.RoboticsFacility),
          Get(Protoss.Observatory)),
        new Pump(Protoss.Observer, 1))),
    new If(
      new And(
        new SafeAtHome,
        new MiningBasesAtLeast(2),
        new Or(
          new EnemiesAtLeast(1, Zerg.Lurker),
          new EnemiesAtLeast(1, Zerg.LurkerEgg))),
      new Parallel(
        new Pump(Protoss.Observer, 3),
        new UpgradeContinuously(Protoss.ObserverSpeed))))
  
  class ReactToMutalisks extends If(
    new EnemyMutalisks,
    new Parallel(
      new Build(
        Get(1, Protoss.Assimilator),
        Get(1, Protoss.CyberneticsCore)),
      new Parallel(
        new PumpRatio(Protoss.Corsair, 1, 8,   Seq(Enemy(Zerg.Mutalisk, 0.8))),
        new PumpRatio(Protoss.Dragoon, 0, 10,  Seq(Enemy(Zerg.Mutalisk, 1.25), Friendly(Protoss.Corsair, -1.0))),
        new Pump(Protoss.Stargate, 1),
        new Build(Get(Protoss.DragoonRange)))))

  class AddEarlyCannons extends If(
    new And(
      new UnitsAtLeast(1, Protoss.Forge),
      new UnitsAtMost(3, Protoss.Gateway, complete = true),
      new UnitsAtMost(8, UnitMatchWarriors)),
    new Parallel(
      new PlacementForgeFastExpand,
      new If(
        new EnemyStrategy(With.fingerprints.fourPool),
        new Parallel(
          new Pump(Protoss.PhotonCannon, 3),
          new PumpWorkers,
          new Pump(Protoss.PhotonCannon, 7))),

      new If(
        new And(new UnitsAtLeast(1, Protoss.Gateway), new UnitsAtLeast(2, Protoss.Nexus)),
        new Parallel(
          new If(
            new EnemyStrategy(With.fingerprints.ninePoolGas),
            new Pump(Protoss.PhotonCannon, 5)),
          new If(
            new And(new EnemyStrategy(With.fingerprints.tenHatch), new EnemiesAtLeast(1, Zerg.Extractor)),
            new Pump(Protoss.PhotonCannon, 4)),
          new If(
            new EnemyStrategy(With.fingerprints.ninePool),
            new Pump(Protoss.PhotonCannon, 3)),
          new If(
            new EnemyHasUpgrade(Zerg.ZerglingSpeed),
            new Pump(Protoss.PhotonCannon, 5)),
          new PumpRatio(Protoss.PhotonCannon, 1, 8,
            Seq(
              Enemy(Zerg.Zergling, 0.416), // Highest ratio that doesn't produce a third cannon vs. 6
              Enemy(Zerg.Hydralisk, 0.75),
              Friendly(Protoss.Zealot, -1.0)))))))

  class AddGateways extends Parallel(
    new IfOnMiningBases(1, new Build(Get(4, Protoss.Gateway))),
    new IfOnMiningBases(2, new Build(Get(9, Protoss.Gateway))),
    new IfOnMiningBases(3, new Build(Get(13, Protoss.Gateway))),
    new IfOnMiningBases(4, new Build(Get(18, Protoss.Gateway))),
    new IfOnMiningBases(5, new Build(Get(24, Protoss.Gateway))))
  
  class TrainAndUpgradeArmy extends Parallel(
    new If(
      new EnemyHasShownCloakedThreat,
      new Parallel(
        new Pump(Protoss.Observer, 3),
        new If(
          new SafeAtHome,
          new UpgradeContinuously(Protoss.ObserverSpeed))),
      new Pump(Protoss.Observer, 1)),
    
    // Upgrades
    new If(
      new UnitsAtLeast(2, Protoss.Forge),
      new Parallel(
        new UpgradeContinuously(Protoss.GroundDamage),
        new UpgradeContinuously(Protoss.GroundArmor)),
      new If(
        new UnitsAtLeast(1, Protoss.TemplarArchives),
        new If(
          new UpgradeComplete(Protoss.GroundDamage, 3),
          new UpgradeContinuously(Protoss.GroundArmor),
          new UpgradeContinuously(Protoss.GroundDamage)),
        new Parallel(
          new If(
            new UpgradeComplete(Protoss.GroundDamage),
            new Build(Get(Protoss.GroundArmor)),
            new Build(Get(Protoss.GroundDamage)))))),
    new If(
      new Or(
        new UnitsAtLeast(2, Protoss.Carrier),
        new UnitsAtLeast(5, Protoss.Corsair),
        new And(
          new UnitsAtLeast(1, Protoss.Stargate),
          new EnemiesAtLeast(1, Zerg.Mutalisk))),
      new Parallel(
        new UpgradeContinuously(Protoss.AirDamage),
        new UpgradeContinuously(Protoss.AirArmor))),

    // Basic army
    new If(
      new EnemyHasUpgrade(Zerg.OverlordSpeed),
      new Pump(Protoss.DarkTemplar, 3),
      new Pump(Protoss.DarkTemplar, 1)),
    new If(
      new Employing(PvZLateGameReaver),
      new Parallel(
        new PumpShuttleAndReavers(6),
        new If(
          new Or(
            new EnemyHasShown(Zerg.Scourge),
            new EnemyHasShown(Zerg.Mutalisk),
            new EnemiesAtLeast(1, Zerg.Spire)),
          new PumpRatio(Protoss.Corsair, 6, 12, Seq(Friendly(Protoss.Carrier, 3.0)))))),
    new Pump(Protoss.Carrier),
    new Pump(Protoss.Observer, 1),
    new Pump(Protoss.Arbiter, 12),
    new If(
      new Or(
        new Employing(PvZLateGameTemplar),
        new TechStarted(Protoss.PsionicStorm)),
      new PumpRatio(Protoss.HighTemplar, 1, 20, Seq(Friendly(UnitMatchWarriors, 0.3)))),
    new PumpRatio(Protoss.Dragoon, 1, 100, Seq(
      Enemy(Zerg.Lurker, 1.0),
      Enemy(Zerg.Mutalisk, 1.0),
      Friendly(Protoss.Zealot, 0.5),
      Friendly(Protoss.Archon, -1.0),
      Friendly(Protoss.Corsair, -1.0))),
    new PumpRatio(Protoss.Corsair, 1, 12, Seq(Enemy(Zerg.Mutalisk, 1.0))),
    new If(
      new Employing(PvZLateGameReaver),
      new Pump(Protoss.Dragoon),
      new Pump(Protoss.HighTemplar)),
    new Pump(Protoss.Zealot)
  )

  class Eject4PoolScout extends If(new FrameAtMost(GameTime(2, 30)()), new EjectScout(Protoss.Probe))
}
