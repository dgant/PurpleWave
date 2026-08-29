package Micro.Actions.Terran

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Land extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.unitClass.isFlyingBuilding
    && unit.flying
    && unit.intent.toLand.isDefined)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.intent.toLand.foreach(Commander.land(unit, _))
  }
}
