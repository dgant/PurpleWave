package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object OccupyBunker extends Action {
  
  override def allowed(unit: FriendlyUnitInfo) = (
    unit.agent.toForm.isDefined
    && Bunk.classAllowedToBunk(unit)
    && unit.transport.isEmpty
    && unit.matchups.threatsInRange.isEmpty)
  
  override def perform(unit: FriendlyUnitInfo) {
    val destination = unit.agent.toForm.get
    
    val bunkers = With.units
      .inTileRadius(destination.tileIncluding, 5)
      .filter(bunker =>
        bunker.isOurs
        && bunker.complete
        && bunker.is(Terran.Bunker)
        && bunker.friendly.get.loadedUnits.size < 4)
  
    ByOption.minBy(bunkers)(_.pixelDistanceFast(unit))
      .foreach(bunker => With.commander.rightClick(unit, bunker))
  }
}
