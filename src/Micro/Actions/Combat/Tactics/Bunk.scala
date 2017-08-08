package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Bunk extends Action {
  
  // Firebats, Medics, and SCVs can enter bunkers too but it's less obvious when they should
  private def allowedToBunk = Vector(
    Terran.Marine,
    Terran.Ghost
  )
  
  private def openBunkersFor(forUnit: FriendlyUnitInfo): Seq[UnitInfo] = {
    forUnit.matchups.allies.filter(ally => ally.is(Terran.Bunker) && ally.friendly.get.loadedUnits.size < 4)
  }
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove                            &&
    allowedToBunk.contains(unit.unitClass)  &&
    openBunkersFor(unit).nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val openBunkers         = openBunkersFor(unit)
    val openBunkersEngaged  = openBunkers.filter(_.matchups.targetsInRange.nonEmpty)
    val openBunkerToEnter   = ByOption.minBy(openBunkersEngaged)(_.pixelDistanceFast(unit))
    
    if (openBunkerToEnter.isDefined) {
      With.commander.rightClick(unit, openBunkerToEnter.get)
    }
  }
}
