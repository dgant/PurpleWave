package Micro.Actions.Terran

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Unsiege extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.SiegeTankSieged)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
  
    val visibleTargets = unit.matchups.targetsInRange.filter(_.visible)
    if (visibleTargets.nonEmpty) {
      return
    }
    
    lazy val tile             = unit.tileIncludingCenter
    lazy val home             = With.geography.home.pixelCenter
    lazy val otherSiegeTanks  = unit.matchups.allies.filter(_.unitClass.isSiegeTank)
    lazy val closerSiegeTanks = unit.agent.toTravel.map(destination => otherSiegeTanks.filter(_.pixelDistanceTravelling(destination) < unit.pixelDistanceTravelling(destination) - Siege.spread)).getOrElse(Vector.empty)
    lazy val uncheckedThreats = unit.matchups.threatsInRange.filter(threat => threat.matchups.threatsInRange.isEmpty)
    lazy val gettingPickedOff = visibleTargets.isEmpty && uncheckedThreats.nonEmpty
    
    var shouldUnsiege = false
    
    shouldUnsiege ||= With.grids.chokepoints.get(tile) && tile.zone.owner.isUs
    shouldUnsiege ||= otherSiegeTanks.isEmpty
    shouldUnsiege ||= otherSiegeTanks.nonEmpty && closerSiegeTanks.nonEmpty
    shouldUnsiege ||= closerSiegeTanks.nonEmpty
    shouldUnsiege ||= gettingPickedOff
    shouldUnsiege || unit.agent.shovers.nonEmpty
    
    if (shouldUnsiege) {
      With.commander.useTech(unit, Terran.SiegeMode)
    }
    
  }
}
