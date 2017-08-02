package Micro.Actions.Terran

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Siege extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.SiegeTankUnsieged)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    if (unit.matchups.threats.exists(_.pixelDistanceFast(unit) < 32.0 * 6.0)) {
      return
    }
    if (unit.matchups.framesToLiveDiffused < 24.0 * 5.0) {
      return
    }
    
    var siege = false
    if (unit.agent.toTravel.exists(_.pixelDistanceFast(unit.pixelCenter) < 32.0)) {
      siege = true
    }
    if (unit.matchups.targets.exists(_.pixelDistanceFast(unit) <= 32.0 * 13.0)) {
      siege = true
    }
    if (unit.agent.toTravel.isDefined) {
      val otherSiegeTanks = unit.matchups.allies.filter(_.unitClass.isSiegeTank)
      val closerSiegeTanks = otherSiegeTanks.filter(_.pixelDistanceFast(unit.agent.toTravel.get) < unit.pixelDistanceFast(unit.agent.toTravel.get))
      if (otherSiegeTanks.nonEmpty && closerSiegeTanks.isEmpty)
        siege = true
    }
    
    if (siege) {
      With.commander.useTech(unit, Terran.SiegeMode)
    }
    
  }
}
