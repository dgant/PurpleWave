package Micro.Actions.Protoss

import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object BeACorsair extends Action {
  
  // Carriers are really finicky.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.aliveAndComplete           &&
    unit.is(Protoss.Corsair)        &&
    unit.matchups.enemies.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    //val scaryScourge = unit.matchups.enemies.filter()
  }
}
