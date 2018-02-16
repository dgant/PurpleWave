package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Army.{Attack, ConsiderAttacking}
import Planning.Plans.Compound._
import Planning.Plans.Predicates.Reactive.{EnemyCarriers, EnemyDarkTemplarExists, EnemyDarkTemplarPossible}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Economy.GasAtMost
import Planning.Plans.Predicates.Milestones._
import ProxyBwapi.Races.Protoss

object PvPIdeas {
  
  class Crummy2GateDetection extends And(
    new EnemyUnitsAtLeast(2,  Protoss.Gateway,          complete = true),
    new EnemyUnitsAtMost(0,   Protoss.CyberneticsCore,  complete = true),
    new EnemyHasShown(Protoss.Zealot, 3),
    new Not(new EnemyHasShown(Protoss.Dragoon)))
  
  class AttackSafely extends If(
    new And(
      new UnitsAtLeast(1, Protoss.Dragoon),
      new EnemyUnitsAtMost(0, Protoss.Dragoon),
      new Not(new EnemyHasUpgrade(Protoss.ZealotSpeed))),
    new Attack,
    new ConsiderAttacking)
  
  class ReactToDarkTemplarEmergencies extends Parallel(new ReactToDarkTemplarExisting, new ReactToDarkTemplarPossible)
  
  class ReactToDarkTemplarPossible extends If(
    new EnemyDarkTemplarPossible,
    new Parallel(
      new If(
        new UnitsAtLeast(1, Protoss.RoboticsFacility),
        new Build(
          RequestAtLeast(1, Protoss.Observatory),
          RequestAtLeast(1, Protoss.Observer)),
        new Build(
          RequestAtLeast(1, Protoss.Forge),
          RequestAtLeast(1, Protoss.PhotonCannon),
          RequestAtLeast(1, Protoss.RoboticsFacility)))))
  
  class ReactToDarkTemplarExisting extends If(
    new EnemyDarkTemplarExists,
    new FlipIf(
      new UnitsAtLeast(1, Protoss.Observatory),
      new Build(RequestAtLeast(1, Protoss.Observer)),
      new Build(
        RequestAtLeast(1, Protoss.Forge),
        RequestAtLeast(2, Protoss.PhotonCannon),
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory))))
  
  class BuildDragoonsOrZealots extends If(
    new Or(
      new UnitsAtMost(0, Protoss.CyberneticsCore,  complete = true),
      new UnitsAtMost(0, Protoss.Assimilator,      complete = true),
      new GasAtMost(30),
      new And(
        new GasAtMost(100),
        new Check(() => With.self.minerals > With.self.gas * 5)),
      new And(
        new UpgradeComplete(Protoss.ZealotSpeed, 1, Protoss.Zealot.buildFrames),
        new UnitsAtLeast(12, Protoss.Dragoon),
        new Not(new EnemyCarriers))),
    new TrainContinuously(Protoss.Zealot),
    new TrainContinuously(Protoss.Dragoon))
  
}
