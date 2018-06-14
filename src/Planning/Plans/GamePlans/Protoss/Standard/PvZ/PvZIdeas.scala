package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Macro.Automatic.{Enemy, Pump, TrainMatchingRatio, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.BuildCannonsAtExpansions
import Planning.Plans.Predicates.Economy.GasAtLeast
import Planning.Plans.Predicates.Milestones.{EnemyHasShownCloakedThreat, _}
import Planning.Plans.Predicates.Reactive.EnemyMutalisks
import Planning.Plans.Predicates.{SafeAtHome, SafeToMoveOut}
import ProxyBwapi.Races.{Protoss, Zerg}

object PvZIdeas {
  
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
  
  class TakeSafeThirdBase extends If(
    new Or(
      new And(
        new SafeToMoveOut,
        new UnitsAtLeast(15, UnitMatchWarriors, complete = true)),
      new And(
        new SafeAtHome,
        new UnitsAtLeast(20, UnitMatchWarriors, complete = true)),
      new UnitsAtLeast(25, UnitMatchWarriors, complete = true)),
    new RequireMiningBases(3))
  
  class TakeSafeFourthBase extends If(
    new Or(
      new And(
        new SafeToMoveOut,
        new UnitsAtLeast(30, UnitMatchWarriors, complete = true)),
      new And(
        new SafeAtHome,
        new UnitsAtLeast(35, UnitMatchWarriors, complete = true)),
      new UnitsAtLeast(40, UnitMatchWarriors, complete = true)),
    new RequireMiningBases(4))
  
  class ReactToLurkers extends Parallel(
    new If(
      new Or(
        new EnemyHasShownCloakedThreat,
        new And(
          new SafeAtHome,
          new EnemyHasShown(Zerg.Hydralisk),
          new EnemyHasShown(Zerg.Lair))),
      new Parallel(
        new Build(
          Get(1, Protoss.CyberneticsCore),
          Get(1, Protoss.RoboticsFacility),
          Get(1, Protoss.Observatory)),
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
        new TrainMatchingRatio(Protoss.Corsair, 3, 8,   Seq(Enemy(Zerg.Mutalisk, 0.9))),
        new TrainMatchingRatio(Protoss.Dragoon, 0, 10,  Seq(Enemy(Zerg.Mutalisk, 1.25))),
        new Pump(Protoss.Stargate, 1),
        new Build(Get(Protoss.DragoonRange)))))
  
  class AddEarlyCannons extends If(
    new And(
      new UnitsAtLeast(1, Protoss.Forge),
      new UnitsAtMost(3, Protoss.Gateway, complete = true),
      new UnitsAtMost(8, UnitMatchWarriors)),
    new Parallel(
      new PlacementForgeFastExpand,
      new TrainMatchingRatio(Protoss.PhotonCannon, 2, 8,
        Seq(
          Enemy(Zerg.Zergling, 0.3),
          Enemy(Zerg.Hydralisk, 0.75)))))
  
  class AddGateways extends Parallel(
    new IfOnMiningBases(1, new Build(Get(4, Protoss.Gateway))),
    new IfOnMiningBases(2, new Build(Get(8, Protoss.Gateway))),
    new IfOnMiningBases(3, new Build(Get(12, Protoss.Gateway))),
    new IfOnMiningBases(4, new Build(Get(16, Protoss.Gateway))),
    new IfOnMiningBases(5, new Build(Get(20, Protoss.Gateway))))
  
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
      new UnitsAtLeast(1, Protoss.TemplarArchives),
      new Parallel(
        new If(
          new Or(
            new UnitsAtLeast(2, Protoss.Forge),
            new UpgradeComplete(Protoss.GroundDamage, 3)),
          new UpgradeContinuously(Protoss.GroundArmor),
          new UpgradeContinuously(Protoss.GroundDamage))),
      new Parallel(
        new If(
          new UpgradeComplete(Protoss.GroundDamage),
          new Build(Get(Protoss.GroundArmor)),
          new Build(Get(Protoss.GroundDamage))))),
    
    // Basic army
    new Pump(Protoss.DarkTemplar, 1),
    new IfOnMiningBases(2, new Pump(Protoss.Reaver, 6)),
    new Pump(Protoss.Observer, 1),
    new If(
      new Check(() => With.units.countOurs(Protoss.Dragoon) < With.units.countEnemy(Zerg.Lurker) * 3),
      new Pump(Protoss.Dragoon),
      new If(
        new Check(() => With.units.countOurs(Protoss.Dragoon) < With.units.countOurs(Protoss.Zealot) * 3 - 24),
        new Pump(Protoss.Dragoon, maximumConcurrentlyRatio = 0.5))),
    new If(
      new Or(
        new UnitsAtMost(10, Protoss.HighTemplar),
        new UnitsAtMost(8, Protoss.Archon)),
      new If(
        new GasAtLeast(200),
        new Pump(Protoss.HighTemplar, 20, 3),
        new Pump(Protoss.HighTemplar, 20, 1))),
    new BuildCannonsAtExpansions(5),
    new Pump(Protoss.Zealot),
    new If(
      new And(
        new SafeAtHome,
        new UnitsAtMost(8, Zerg.Hydralisk),
        new UnitsAtMost(1, Zerg.SporeColony)),
      new Pump(Protoss.Corsair, 6),
      new Pump(Protoss.Corsair, 1))
  )
}
