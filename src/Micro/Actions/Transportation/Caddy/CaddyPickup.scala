package Micro.Actions.Transportation.Caddy

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object CaddyPickup extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.isTransport && unit.canMove

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val targets = unit.teammates.toVector.flatMap(_.friendly).filter(unit.canTransport)
  }
}
