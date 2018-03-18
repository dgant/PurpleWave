package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Compound.{And, If, Or, Parallel}
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Macro.Automatic.{MatchingRatio, TrainContinuously, TrainMatchingRatio}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.{SafeAtHome, SafeToAttack}
import ProxyBwapi.Races.{Protoss, Zerg}

object PvZIdeas {
  
  class TakeSafeNatural extends If(
    new Or(
      new And(
        new SafeToAttack,
        new UnitsAtLeast(6, UnitMatchWarriors, complete = true)),
      new And(
        new SafeAtHome,
        new UnitsAtLeast(10, UnitMatchWarriors, complete = true)),
      new UnitsAtLeast(14, UnitMatchWarriors, complete = true)),
    new RequireMiningBases(2))
  
  class TakeSafeThirdBase extends If(
    new Or(
      new And(
        new SafeToAttack,
        new UnitsAtLeast(12, UnitMatchWarriors, complete = true)),
      new And(
        new SafeAtHome,
        new UnitsAtLeast(15, UnitMatchWarriors, complete = true)),
      new UnitsAtLeast(20, UnitMatchWarriors, complete = true)),
    new RequireMiningBases(3))
  
  class TakeSafeFourthBase extends If(
    new Or(
      new And(
        new SafeToAttack,
        new UnitsAtLeast(18, UnitMatchWarriors, complete = true)),
      new And(
        new SafeAtHome,
        new UnitsAtLeast(24, UnitMatchWarriors, complete = true)),
      new UnitsAtLeast(30, UnitMatchWarriors, complete = true)),
    new RequireMiningBases(4))
  
  class BuildDetectionForLurkers extends Parallel(
    new If(
      new Or(
        new EnemyHasShown(Zerg.Lurker),
        new EnemyHasShown(Zerg.LurkerEgg),
        new And(
          new SafeAtHome,
          new EnemyHasShown(Zerg.Hydralisk),
          new EnemyHasShown(Zerg.Lair))),
      new Parallel(
        new Build(
          RequestAtLeast(1, Protoss.CyberneticsCore),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory)),
        new TrainContinuously(Protoss.Observer, 1))),
    new If(
      new And(
        new SafeAtHome,
        new MiningBasesAtLeast(2),
        new Or(
          new EnemyUnitsAtLeast(1, Zerg.Lurker),
          new EnemyUnitsAtLeast(1, Zerg.LurkerEgg))),
      new Parallel(
        new TrainContinuously(Protoss.Observer, 3),
        new UpgradeContinuously(Protoss.ObserverSpeed))))
  
  class AddEarlyCannons extends If(
    new UnitsAtMost(2, Protoss.Gateway, complete = true),
    new Parallel(
      new PlacementForgeFastExpand,
      new TrainMatchingRatio(Protoss.PhotonCannon, 2, 6,
        Seq(
          MatchingRatio(Zerg.Zergling, 0.3),
          MatchingRatio(Zerg.Hydralisk, 0.75)))))
  
  class AddGateways extends Parallel(
    new IfOnMiningBases(1, new Build(RequestAtLeast(4, Protoss.Gateway))),
    new IfOnMiningBases(2, new Build(RequestAtLeast(8, Protoss.Gateway))),
    new IfOnMiningBases(3, new Build(RequestAtLeast(12, Protoss.Gateway))),
    new IfOnMiningBases(4, new Build(RequestAtLeast(16, Protoss.Gateway))),
    new IfOnMiningBases(5, new Build(RequestAtLeast(20, Protoss.Gateway))))
}
