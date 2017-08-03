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
  
    val visibleTargets = unit.matchups.targetsInRange.filter(_.visible)
    if (visibleTargets.nonEmpty) {
      return
    }
    
    var unsiege = false
    
    if (unit.agent.toTravel.isDefined) {
  
      lazy val tile             = unit.tileIncludingCenter
      lazy val home             = With.geography.home.pixelCenter
      lazy val otherSiegeTanks  = unit.matchups.allies.filter(_.unitClass.isSiegeTank)
      lazy val closerSiegeTanks = otherSiegeTanks.filter(_.pixelDistanceFast(unit.agent.toTravel.get) < unit.pixelDistanceFast(unit.agent.toTravel.get) - Siege.spread)
      lazy val uncheckedThreats = unit.matchups.threatsInRange.filter(threat => threat.matchups.threatsInRange.isEmpty)
      lazy val gettingPickedOff = visibleTargets.isEmpty && uncheckedThreats.nonEmpty
      
      unsiege ||= With.grids.chokepoints.get(tile) && tile.zone.owner.isUs
      unsiege ||= otherSiegeTanks.isEmpty
      unsiege ||= otherSiegeTanks.nonEmpty && closerSiegeTanks.nonEmpty
      unsiege ||= closerSiegeTanks.nonEmpty
      unsiege ||= gettingPickedOff
    }
    
    if (unsiege) {
      With.commander.useTech(unit, Terran.SiegeMode)
    }
    
  }
}
