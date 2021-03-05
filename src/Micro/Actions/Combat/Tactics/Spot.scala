package Micro.Actions.Combat.Tactics

import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Spot extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.flying
    && ! unit.unitClass.isTransport
    && ! unit.canAttack
    && (unit.matchups.pixelsOfEntanglement > -32 || unit.totalHealth > 500 || (unit.cloaked && unit.matchups.enemyDetectors.isEmpty))
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    lazy val tankVulnerableAllyCentroid = PurpleMath.centroid(unit.alliesSquad.filter( ! _.flying).map(_.pixel))
    val tanksToSpot = unit.enemiesSquad.filter(t => t.is(Terran.SiegeTankSieged) && t.matchups.enemies.forall(a => a == unit || ! a.canSee(t)))
    val tankToSpot = ByOption.minBy(tanksToSpot)(_.pixelDistanceSquared(tankVulnerableAllyCentroid))

    lazy val enemyToSpot = unit.enemiesSquad

    val goal: Option[Pixel] = destinationFromTeammates(unit, unit.alliesSquad)
      .map(p =>
        if ( ! p.tile.visible) p else
          ByOption.minBy(unit.enemiesSquadOrBattle.filter( ! _.visible))(_.pixelDistanceCenter(p)).orElse(
            ByOption.minBy(unit.enemiesSquadOrBattle)(_.pixelDistanceCenter(p)))
              .map(_.pixel)
              .getOrElse(p))

    if (goal.isDefined) {
      unit.agent.toTravel = goal
      Potshot.delegate(unit)
      Commander.move(unit)
    }
  }

  protected def destinationFromTeammates(unit: FriendlyUnitInfo, teammates: Iterable[UnitInfo]): Option[Pixel] = {
    ByOption.minBy(
      unit.alliesSquad
        .filter(_.canAttack)
        .flatMap(_.friendly)
        .map(u => u.pixel.project(u.agent.destination, unit.sightPixels)))(_.pixelDistance(unit.agent.destination))
  }
}
