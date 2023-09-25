package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object OccupyBunker extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    With.self.isTerran
    && EmergencyBunk.classAllowedToBunk(unit)
    && unit.transport.isEmpty
    && unit.matchups.threatsInRange.isEmpty)
  
  override def perform(unit: FriendlyUnitInfo): Unit = {
    
    val bunkers = With.units
      .inTileRadius(unit.agent.origin().tile, 5)
      .filter(bunker =>
        Terran.Bunker(bunker)
        && bunker.isOurs
        && bunker.complete
        && bunker.friendly.get.loadedUnits.size < 4)
  
    Maff.minBy(bunkers)(_.pixelDistanceEdge(unit)).foreach(Commander.rightClick(unit, _))
  }
}
