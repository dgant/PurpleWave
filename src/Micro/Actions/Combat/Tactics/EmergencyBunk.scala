package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object EmergencyBunk extends Action {
  
  // Firebats, Medics, and SCVs can enter bunkers too but it's less obvious when they should
  def classAllowedToBunk(unit: FriendlyUnitInfo): Boolean = unit.isAny(Terran.Marine, Terran.Ghost)
  
  def openBunkersFor(forUnit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    if (classAllowedToBunk(forUnit))
      forUnit.alliesBattle.filter(ally => ally.is(Terran.Bunker) && ally.friendly.get.loadedUnits.size < 4)
    else
      Iterable.empty
  }
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    With.self.isTerran && unit.canMove && openBunkersFor(unit).nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val openBunkers         = openBunkersFor(unit)
    val openBunkersEngaged  = openBunkers.filter(_.matchups.targetsInRange.nonEmpty)
    val openBunkersInForm   = openBunkers.filter(bunker => unit.agent.safety.pixelDistance(bunker.pixel) < 32.0 * 10.0)
    val openBunkerToEnter   = Maff.minBy(openBunkersEngaged)(_.pixelDistanceEdge(unit))
    
    if (openBunkerToEnter.isDefined) {
      Commander.rightClick(unit, openBunkerToEnter.get)
    }
  }
}
