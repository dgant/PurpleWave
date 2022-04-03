package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.Commander
import Utilities.UnitMatchers.{MatchAnd, MatchComplete}
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object StrategicNuke extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.agent.toNuke.isDefined
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val target = unit.agent.toNuke.get
    unit.agent.toTravel = Some(target)
    
    lazy val haveANuke = With.units.existsOurs(MatchAnd(Terran.NuclearMissile, MatchComplete))
    if (unit.pixelDistanceCenter(target) < 11.0 * 32.0 && haveANuke) {
      Commander.useTechOnPixel(unit, Terran.NuclearStrike, target)
    }
  }
}
