package Micro.Actions.Transportation

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object DropOff extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.isTransport                                  &&
    unit.loadedUnits.nonEmpty                         &&
    With.grids.walkable.get(unit.tileIncludingCenter)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val fastestPassenger  = unit.loadedUnits.maxBy(_.topSpeed)
    val inZone            = unit.agent.toTravel.forall(_.zone == unit.zone)
    val onRunway          = unit.agent.toTravel.forall(unit.pixelDistanceCenter(_) < 32.0 * 6.0)
    val inHurry           = unit.matchups.framesToLiveDiffused < (24 + With.reaction.agencyMax) * (1 +  unit.loadedUnits.size)
    
    if (inZone && (onRunway || inHurry)) {
      With.commander.unload(unit, unit.loadedUnits.head)
    }
  }
}
