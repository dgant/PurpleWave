package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Compound._
import Planning.Plans.Information.Reactive.{EnemyCarriers, EnemyDarkTemplarExists, EnemyDarkTemplarPossible}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.{UpgradeComplete, UnitsAtLeast, UnitsAtMost}
import ProxyBwapi.Races.Protoss

object PvPIdeas {
  
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
    new If(
      new UnitsAtLeast(1, Protoss.Observatory),
      new Build(RequestAtLeast(1, Protoss.Observer))),
    new If(
      new UnitsAtLeast(1, Protoss.Forge),
      new Build(RequestAtLeast(2, Protoss.PhotonCannon))))
  
  class BuildDragoonsOrZealots extends If(
    new Or(
      new UnitsAtMost(0, Protoss.CyberneticsCore,  complete = true),
      new UnitsAtMost(0, Protoss.Assimilator,      complete = true),
      new Check(() => With.self.gas < 30),
      new Check(() => With.self.gas < 100 && With.self.minerals > With.self.gas * 5),
      new And(
        new UpgradeComplete(Protoss.ZealotSpeed, Protoss.Zealot.buildFrames),
        new UnitsAtLeast(12, Protoss.Dragoon),
        new Not(new EnemyCarriers))),
    new TrainContinuously(Protoss.Zealot),
    new TrainContinuously(Protoss.Dragoon))
  
}
