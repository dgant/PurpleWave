package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Bunk extends Action {
  
  // Firebats, Medics, and SCVs can enter bunkers too but it's less obvious when they should
  def allowedToBunk(unit: FriendlyUnitInfo): Boolean = Vector(
    Terran.Marine,
    Terran.Ghost
  ).contains(unit.unitClass)
  
  def openBunkersFor(forUnit: FriendlyUnitInfo): Seq[UnitInfo] = {
    if (allowedToBunk(forUnit))
      forUnit.matchups.allies.filter(ally => ally.is(Terran.Bunker) && ally.friendly.get.loadedUnits.size < 4)
    else
      Seq.empty
  }
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && openBunkersFor(unit).nonEmpty
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
