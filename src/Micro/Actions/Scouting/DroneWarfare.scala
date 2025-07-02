package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.Commander
import Micro.Targeting.Target
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object DroneWarfare extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.intent.toScoutTiles.nonEmpty && With.blackboard.droneWarfare()

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Target.choose(unit)
    Commander.attack(unit)
  }
}
