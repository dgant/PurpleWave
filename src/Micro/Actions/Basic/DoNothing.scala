package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object DoNothing extends Action {
  override protected def perform(unit: FriendlyUnitInfo): Unit = { Commander.sleep(unit, 1) }
}
