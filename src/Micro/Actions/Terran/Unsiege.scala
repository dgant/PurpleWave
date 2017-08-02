package Micro.Actions.Terran

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Unsiege extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.SiegeTankSieged)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    if (unit.matchups.targets.exists(t => t.pixelDistanceFast(unit) < 32.0 * 13.0 && t.pixelDistanceFast(unit) > 32.0 * 3.0)) {
      return
    }
    
    var unsiege = false
    
    if (unit.agent.toTravel.isDefined) {
      val otherSiegeTanks = unit.matchups.allies.filter(_.unitClass.isSiegeTank)
      val closerSiegeTanks = otherSiegeTanks.filter(_.pixelDistanceFast(unit.agent.toTravel.get) < unit.pixelDistanceFast(unit.agent.toTravel.get))
      if (otherSiegeTanks.nonEmpty && closerSiegeTanks.nonEmpty) {
        unsiege = true
      }
      
      val home = With.geography.home.pixelCenter
      if (unit.agent.toTravel.exists(_.pixelDistanceFast(home) < unit.pixelDistanceFast(With.geography.home.pixelCenter) - 48)) {
        unsiege = true
      }
    }
    
    if (unsiege) {
      With.commander.useTech(unit, Terran.SiegeMode)
    }
    
  }
}
