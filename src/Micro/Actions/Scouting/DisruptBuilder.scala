package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import bwapi.Race

object DisruptBuilder extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    With.enemies.exists(_.race == Race.Terran) &&
    unit.matchups.enemies.exists(_.constructing)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
  }
  
  
}
