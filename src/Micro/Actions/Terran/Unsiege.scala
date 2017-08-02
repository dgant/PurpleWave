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
    
    if (unit.matchups.targets.exists(unit.inRangeToAttackFast)) {
      return
    }
    
    var unsiege = false
    
    if (unit.agent.toTravel.isDefined) {
  
      lazy val tile             = unit.tileIncludingCenter
      lazy val home             = With.geography.home.pixelCenter
      lazy val otherSiegeTanks  = unit.matchups.allies.filter(_.unitClass.isSiegeTank)
      lazy val closerSiegeTanks = otherSiegeTanks.filter(_.pixelDistanceFast(unit.agent.toTravel.get) < unit.pixelDistanceFast(unit.agent.toTravel.get))
      lazy val gettingPickedOff = unit.matchups.targetsInRange.isEmpty && unit.matchups.threatsInRange.nonEmpty
      
      unsiege ||= With.grids.chokepoints.get(tile) && tile.zone.owner.isUs
      unsiege ||= otherSiegeTanks.isEmpty
      unsiege ||= otherSiegeTanks.nonEmpty && closerSiegeTanks.nonEmpty
      unsiege ||= unit.agent.toTravel.exists(_.pixelDistanceFast(home) < unit.pixelDistanceFast(With.geography.home.pixelCenter) - 48.0)
      unsiege ||= gettingPickedOff
    }
    
    if (unsiege) {
      With.commander.useTech(unit, Terran.SiegeMode)
    }
    
  }
}
