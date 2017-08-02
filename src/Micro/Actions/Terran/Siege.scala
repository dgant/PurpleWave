package Micro.Actions.Terran

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Siege extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.SiegeTankUnsieged) && unit.player.hasTech(Terran.SiegeMode)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    if (unit.matchups.threats.exists(_.pixelDistanceFast(unit) < 32.0 * 6.0)) {
      return
    }
    if (unit.matchups.framesToLiveDiffused < 24.0 * 5.0) {
      return
    }
    
    var siege = false
    lazy val atDestination        = unit.agent.toTravel.exists(_.pixelDistanceFast(unit.pixelCenter) < 32.0)
    lazy val targetsExist         = unit.matchups.targets.exists(t => t.topSpeed > 0 && t.pixelDistanceFast(unit) <= 32.0 * 13.0)
    lazy val otherSiegeTanks      = unit.matchups.allies.filter(_.unitClass.isSiegeTank)
    lazy val closerSiegeTanks     = otherSiegeTanks.filter(_.pixelDistanceTravelling(unit.agent.toTravel.get) < unit.pixelDistanceTravelling(unit.agent.toTravel.get))
    lazy val isClosestTankInPush  = unit.agent.toTravel.isDefined && otherSiegeTanks.nonEmpty && closerSiegeTanks.isEmpty
    
    siege ||= atDestination
    siege ||= targetsExist
    siege ||= isClosestTankInPush
    
    if (siege) {
      With.commander.useTech(unit, Terran.SiegeMode)
    }
    
  }
}
