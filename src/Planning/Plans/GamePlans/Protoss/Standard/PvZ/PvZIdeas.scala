package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Macro.Automatic.{Enemy, Pump, PumpMatchingRatio, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, MeldArchons}
import Planning.Predicates.Compound.{And, Check}
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones.{EnemyHasShownCloakedThreat, _}
import Planning.Predicates.Reactive.{EnemyMutalisks, SafeAtHome, SafeToMoveOut}
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.{PvZ4Gate99, PvZ4GateDragoonAllIn}

object PvZIdeas {

  class ConditionalAttack extends If(
    new Or(
      new SafeToMoveOut,
      new BasesAtLeast(3),
      new Employing(PvZ4Gate99, PvZ4GateDragoonAllIn)),
    new Attack)

  class MeldArchonsUntilStorm extends If(
    new TechStarted(Protoss.PsionicStorm),
    new MeldArchons(49) { override def maximumTemplar = 8 },
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
        new PumpMatchingRatio(Protoss.Corsair, 1, 8,   Seq(Enemy(Zerg.Mutalisk, 0.9))),
        new PumpMatchingRatio(Protoss.Dragoon, 0, 10,  Seq(Enemy(Zerg.Mutalisk, 1.25))),
        new Pump(Protoss.Stargate, 1),
        new Build(Get(Protoss.DragoonRange)))))
  
  class AddEarlyCannons extends If(
    new And(
      new UnitsAtLeast(1, Protoss.Forge),
      new UnitsAtMost(3, Protoss.Gateway, complete = true),
      new UnitsAtMost(8, UnitMatchWarriors)),
    new Parallel(
      new PlacementForgeFastExpand,
      new PumpMatchingRatio(Protoss.PhotonCannon, 2, 8,
        Seq(
          Enemy(Zerg.Zergling, 0.3),
          Enemy(Zerg.Hydralisk, 0.75)))))
  
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
