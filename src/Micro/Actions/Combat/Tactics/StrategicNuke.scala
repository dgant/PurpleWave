package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object StrategicNuke extends Action {
  override def allowed(unit: FriendlyUnitInfo) = {
    unit.agent.toNuke.isDefined
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val target = unit.agent.toNuke.get
    unit.agent.toTravel = Some(target)
    
    lazy val haveANuke = With.units.ours.exists(u => u.aliveAndComplete && u.is(Terran.NuclearMissile))
    if (unit.pixelDistanceCenter(target) < 11.0 * 32.0 && haveANuke) {
      With.commander.useTechOnPixel(unit, Terran.NuclearStrike, target)
    }
  }
}
