package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Agency.Explosion
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable.ListBuffer

object Duck extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    unit.agent.explosions ++= getDodgables(unit)
    
    if (unit.agent.explosions.exists(explosion =>
      explosion.radius >=
      explosion.center.pixelDistanceFast(unit.pixelCenter) + 32.0)) {
      
      // TODO: Get out of the way!
    }
  }
  
  private def getDodgables(unit: FriendlyUnitInfo): Iterable[Explosion] = {
    val output = new ListBuffer[Explosion]
    
    if ( ! unit.flying) {
      output ++= unit.matchups.threats
        .filter(threat =>
          threat.target.isDefined         &&
          ! threat.target.contains(unit)  &&
          (
            threat.is(Protoss.Scarab)     ||
            threat.is(Terran.SpiderMine)
          ))
        .map(threat => Explosion(
          threat.target.get.pixelCenter,
          32.0 * 2.0,
          threat.damageOnNextHitAgainst(unit)))
    }
    
    output
  }
}
