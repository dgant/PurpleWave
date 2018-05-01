package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers._
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.{BuildCannonsAtBases, MeldArchons}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.GasAtMost
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Reactive.{EnemyBasesAtLeast, EnemyCarriers, EnemyDarkTemplarExists, EnemyDarkTemplarPossible}
import Planning.Plans.Predicates.Scenarios.WeAreBeingProxied
import Planning.Plans.Predicates.{SafeAtHome, SafeToAttack}
import ProxyBwapi.Races.Protoss

object PvPIdeas {
  
  class EnemyCarriersOnly extends And(
    new EnemyCarriers,
    new EnemyUnitsAtMost(6, UnitMatchAnd(UnitMatchWarriors,  UnitMatchNot(UnitMatchMobileFlying))))
  
  class AttackWithDarkTemplar extends If(
    new Or(
      new EnemyUnitsNone(Protoss.Observer),
      new EnemyBasesAtLeast(3)),
    new Attack { attackers.get.unitMatcher.set(Protoss.DarkTemplar) })
  
  class AttackSafely extends If(
    new Or(
      new And(
        new EnemyUnitsAtLeast(1, Protoss.Forge),
        new EnemyUnitsAtMost(0, UnitMatchWarriors)),
      new And(
        new Or(
          new UnitsAtLeast(1, Protoss.Observer, complete = true),
          new Not(new EnemyDarkTemplarExists)),
        new Or(
          new SafeToAttack,
          new EnemyBasesAtLeast(3),
          new And(
            new UnitsAtLeast(1, Protoss.Dragoon),
            new EnemyUnitsAtMost(0, Protoss.Dragoon),
            new Not(new EnemyHasUpgrade(Protoss.ZealotSpeed)))),
        new Or(
          new UnitsAtMost(0, Protoss.Dragoon),
          new UpgradeComplete(Protoss.DragoonRange)),
        new Or(
          new UnitsAtLeast(0, Protoss.Reaver),
          new UnitsAtLeast(1, Protoss.Shuttle),
          new UnitsAtLeast(15, UnitMatchWarriors)),
        new Or(
          new UnitsAtMost(1, Protoss.Nexus),
          new UnitsAtLeast(5, Protoss.Gateway),
          new UnitsAtLeast(20, UnitMatchWarriors)))),
    new Attack)
  
  class ReactToDarkTemplarEmergencies extends Parallel(new ReactToDarkTemplarExisting, new ReactToDarkTemplarPossible)
  class ReactToDarkTemplarPossible extends If(
    new EnemyDarkTemplarPossible,
    new Parallel(
      new If(
        new UnitsAtMost(0, Protoss.Observatory),
        new BuildCannonsAtBases(1)),
      new Build(
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory),
        RequestAtLeast(1, Protoss.Observer))))
  
  class ReactToDarkTemplarExisting extends If(
    new EnemyDarkTemplarExists,
    new Parallel(
      new If(
        new UnitsAtMost(0, Protoss.Observatory),
        new BuildCannonsAtBases(1)),
      new Build(
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory)),
      new TrainContinuously(Protoss.Observer, 3)))
  
  class ReactToFFE extends If(
    new And(
      new Not(new WeAreBeingProxied),
      new EnemyUnitsAtLeast(1, Protoss.PhotonCannon),
      new SafeAtHome),
    new RequireMiningBases(2))
  
  class ReactToExpansion extends If(
    new And(
      new EnemyBasesAtLeast(2),
      new MiningBasesAtMost(1)),
    new Trigger(
      new And(
        new SafeAtHome,
        new UnitsAtLeast(1, Protoss.CyberneticsCore),
        new UnitsAtLeast(1, Protoss.Assimilator)),
      // Match if it we're already on Dragoon tech
      new RequireMiningBases(2),
      // Otherwise, let's go all in with Zealots
      new FlipIf(
        new SafeAtHome,
        new Parallel(
          new TrainContinuously(Protoss.Probe, 19),
          new Build(RequestAtLeast(4, Protoss.Gateway))),
        new Parallel(
          new PvPIdeas.TrainDragoonsOrZealots,
          new UpgradeContinuously(Protoss.DragoonRange)))))
  
  class TakeBase2 extends If(
    new Or(
      new UnitsAtLeast(2, Protoss.Reaver, complete = true),
      new And(
        new SafeAtHome,
        new UnitsAtLeast(8, UnitMatchWarriors, complete = true)),
      new UnitsAtLeast(16, UnitMatchWarriors, complete = true)),
    new RequireMiningBases(2))
  
  class TakeBase3 extends If(
    new Or(
      new UnitsAtLeast(40, UnitMatchWarriors),
      new And(
        new SafeAtHome,
        new Or(
          new EnemyCarriers,
          new EnemyBasesAtLeast(3)))),
    new RequireMiningBases(2))
  
  class MeldArchonsPvP extends MeldArchons(49) {
    override def minimumArchons: Int = Math.min(8, With.units.enemy.count(_.is(Protoss.Zealot)) / 3)
    templar.unitMatcher.set(UnitMatchAnd(Protoss.HighTemplar, UnitMatchEnergyAtMost(75)))
  }
  
  class TrainDragoonsOrZealots extends If(
    new And(
      new Not(new EnemyCarriersOnly),
      new Or(
        new UnitsAtMost(0, Protoss.CyberneticsCore,  complete = true),
        new UnitsAtMost(0, Protoss.Assimilator,      complete = true),
        new GasAtMost(30),
        new And(
          new GasAtMost(100),
          new Check(() => With.self.minerals > With.self.gas * 5)),
        new And(
          new UpgradeComplete(Protoss.ZealotSpeed, 1, Protoss.Zealot.buildFrames),
          new Or(
            new UnitsAtLeast(12, Protoss.Dragoon),
            new Check(() => With.self.minerals > With.self.gas * 3))))),
    new TrainContinuously(Protoss.Zealot),
    new TrainContinuously(Protoss.Dragoon))
    
  class TrainDarkTemplar extends If(
    new And(
      new EnemyUnitsAtMost(0, Protoss.PhotonCannon),
      new EnemyUnitsAtMost(0, Protoss.Observer)),
    new TrainContinuously(Protoss.DarkTemplar, 3),
    new TrainContinuously(Protoss.DarkTemplar, 1))
    
  class TrainArmy extends Parallel(
    new TrainContinuously(Protoss.Carrier),
    new If(
      new And(
        new Not(new EnemyCarriersOnly),
        new UnitsAtMost(0, Protoss.PhotonCannon)),
      new TrainContinuously(Protoss.Observer, 1)),
    new If(
      new Not(new EnemyCarriersOnly),
      new TrainDarkTemplar),
    new If(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new TrainContinuously(Protoss.Arbiter, 8, 2)),
    new If(
      new And(
        new Not(new EnemyCarriersOnly),
        new UnitsAtMost(0, Protoss.TemplarArchives)),
      new TrainContinuously(Protoss.Reaver, 2)),
    new If(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new TrainContinuously(Protoss.HighTemplar, 6, 2)),
    new TrainDragoonsOrZealots,
    new TrainContinuously(Protoss.Observer, 2)
  )
}
