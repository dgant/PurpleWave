package Micro.Actions.Protoss.Shuttle

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShuttleAwait extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = BeShuttle.allowed(shuttle) && shuttle.agent.passengers.isEmpty

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    val roboticsFacility = With.units.ours
      .filter(Protoss.RoboticsFacility)
      .toVector
      .sortBy(_.pixelDistanceCenter(shuttle))
      .sortBy(! _.trainee.exists(Protoss.Reaver))
      .headOption
    roboticsFacility.foreach(robo => {
      val roboCorner = robo.bottomLeftInclusive.add(0, 16)
      shuttle.agent.redoubt.set(roboCorner)
      shuttle.agent.decision.set(roboCorner)
      val framesToRobotics  = shuttle.framesToTravelTo(roboCorner)
      val framesToReaver    = robo.trainee.map(_.remainingCompletionFrames).getOrElse(Protoss.Reaver.buildFrames)
      // Protect the Shuttle unless it's imminently needed to rescue a Reaver
      if (shuttle.matchups.framesOfSafety < 48 && (framesToRobotics < framesToReaver || robo.matchups.enemies.isEmpty)) {
        Retreat(shuttle)
      } else {
        Commander.move(shuttle)
      }
    })
  }
}
