package Micro.Actions.Transportation.Caddy

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShuttleCircle extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = BeAShuttle.allowed(unit) && unit.agent.passengers.isEmpty

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    val roboticsFacility = With.units.ours
      .filter(_.is(Protoss.RoboticsFacility))
      .toVector
      .sortBy(_.pixelDistanceCenter(shuttle))
      .sortBy(! _.trainee.exists(_.is(Protoss.Reaver)))
      .headOption
    roboticsFacility.foreach(robo => {
      val roboCorner = robo.bottomLeft.add(0, 16)
      shuttle.agent.toTravel = Some(roboCorner)
      shuttle.agent.toReturn = Some(roboCorner)
      val framesToRobotics = shuttle.framesToTravelTo(roboCorner)
      val framesToReaver = robo.trainee.map(_.remainingCompletionFrames).getOrElse(Protoss.Reaver.buildFrames)

      // Protect the Shuttle unless it's imminently needed to rescue a Reaver
      if (shuttle.matchups.framesOfSafety < 48 && (framesToRobotics < framesToReaver || robo.matchups.enemies.isEmpty)) {
        Retreat.consider(shuttle)
      } else {
        Commander.move(shuttle)
      }
    })
  }
}
