package Micro.Actions.Transportation.Caddy

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object ShuttleCircle extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = BeAShuttle.allowed(unit) && unit.agent.passengers.isEmpty

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    val roboticsFacility = ByOption.minBy(With.units.ours.view.filter(u => u.is(Protoss.RoboticsFacility)))(_.pixelDistanceCenter(shuttle))
    roboticsFacility.foreach(robo => {
      val roboCorner = robo.bottomLeft
      val framesToRobotics = shuttle.framesToTravelTo(roboCorner)
      val framesToReaver = robo.trainee.map(_.remainingCompletionFrames).getOrElse(Protoss.Reaver.buildFrames)

      // Protect the Shuttle unless it's imminently needed to rescue a Reaver
      if (shuttle.matchups.framesOfSafety < 48 && (framesToRobotics < framesToReaver || robo.matchups.enemies.isEmpty)) {
        Retreat.consider(shuttle)
      } else {
        shuttle.agent.toTravel = Some(roboCorner)
        With.commander.move(shuttle)
      }
    })
  }
}
