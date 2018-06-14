package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Planning.UnitMatchers.UnitMatchWorkers
import ProxyBwapi.UnitInfo.UnitInfo

class GoalEjectScout extends GoalBasic {
  
  override def toString: String = "Eject scouts"
  
  var scout: Option[UnitInfo] = None
  
  override def destination: Pixel = scout.map(_.pixelCenter).getOrElse(With.geography.home.pixelCenter)
  
  override def run() {
    squad.units.foreach(ejector => {
      val target = if (ejector.matchups.targets.forall(_.is(UnitMatchWorkers))) scout else None
      ejector.agent.intend(squad.client, new Intention {
        toTravel = Some(destination)
        toAttack = target
      })
    })
  }
}
