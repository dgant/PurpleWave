package Planning.Plans.GamePlans.Protoss.PvP

import Lifecycle.With
import Macro.Requests.Get
import Mathematics.Maff
import Planning.Plans.Basic.WriteStatus
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Protoss.BuildTowersAtBases
import Planning.Predicates.Compound._
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive._
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

object OldPvPReactions {

  // Fast proxy DT: 5:15
  // More normal timing: Closer to 6:00
  val dtArrivalTime: Int = GameTime(5, 45)()
  class ReactToDarkTemplarEmergencies extends Parallel(new ReactToDarkTemplarExisting, new ReactToDarkTemplarLikely)
  private class ReactToDarkTemplarLikely extends If(
    new EnemyDarkTemplarLikely,
    new Parallel(
      new WriteStatus("ReactToDTLikely"),
      new If(
        new And(
          new UnitsAtMost(0, Protoss.RoboticsFacility),
          new FrameAtLeast(() =>
            dtArrivalTime
            - Maff.max(With.units.ours.view.filter(Protoss.RoboticsFacility).map(_.remainingCompletionFrames)).getOrElse(Protoss.RoboticsFacility.buildFrames)
            - Maff.max(With.units.ours.view.filter(Protoss.Observatory).map(_.remainingCompletionFrames)).getOrElse(Protoss.Observatory.buildFrames)
            - Maff.max(With.units.ours.view.filter(Protoss.Observatory).map(_.remainingCompletionFrames)).getOrElse(Protoss.Observer.buildFrames)),
          new FrameAtLeast(() =>
            dtArrivalTime
            - Protoss.Forge.buildFrames
            - Protoss.PhotonCannon.buildFrames)),
        new Build(Get(Protoss.Forge))),
      new If(
        new And(
          new UnitsAtMost(0, Protoss.Observer, complete = true),
          new UnitsAtLeast(1, Protoss.Forge)),
        new BuildTowersAtBases(1)),
      new If(
        new UnitsAtMost(0, Protoss.Forge),
        new Build(
          Get(Protoss.RoboticsFacility),
          Get(Protoss.Observatory),
          Get(Protoss.Observer)))))

  private class ReactToDarkTemplarExisting extends If(
    new EnemyHasShown(Protoss.DarkTemplar),
    new Parallel(
      new WriteStatus("ReactToDTExisting"),
      new If(
        new UnitsAtMost(0, Protoss.Observer),
        new BuildTowersAtBases(2)),
      new Build(
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Observatory),
        Get(Protoss.Observer)),
      new Pump(Protoss.Observer, 3)))
}
