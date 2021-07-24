package Micro.Actions.Combat.Tactics

import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}


object Spot extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.flying
    && ! unit.unitClass.isTransport
    && ! unit.canAttack
    && unit.alliesSquad.exists(_.canAttack)
    && (unit.matchups.pixelsOfEntanglement > -58 || unit.totalHealth > 500 || (unit.cloaked && unit.matchups.enemyDetectors.isEmpty))
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val from = unit.team.map(_.centroidAir).getOrElse(unit.agent.destination)
    val toSpotEnemy = best(from, unit, unit.enemiesSquad).orElse(best(from, unit, unit.enemiesBattle))
    val toSpot = toSpotEnemy.map(_.pixel).getOrElse(unit.agent.destination)
    unit.agent.toTravel = Some(toSpot)
    Commander.move(unit)
  }

  def best(from: Pixel, spotter: FriendlyUnitInfo, enemies: Iterable[UnitInfo]): Option[UnitInfo] = {
    val invisible = enemies.filterNot(_.visible)
    Maff.minBy(if (invisible.nonEmpty) invisible else enemies)(_.pixelDistanceCenter(from))
  }
}
