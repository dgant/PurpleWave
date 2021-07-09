package Tactics.Squads

import Lifecycle.With
import Mathematics.Maff
import Planning.UnitMatchers.{MatchProxied, MatchWarriors}
import ProxyBwapi.Races.Terran
import Utilities.Minutes

class SquadAttack extends Squad {
  override def toString: String = f"Atk ${vicinity.base.map(_.name).getOrElse(vicinity.zone.name).take(4)}"

  override def run() {
    if (units.isEmpty) return
    chooseVicinity()
    SquadAutomation.targetFormAndSend(this, minToForm = 5)
  }

  protected def chooseVicinity(): Unit = {
    val threatOrigin = With.scouting.threatOrigin
    val centroid = Maff.centroid(units.view.map(_.pixel)).tile
    val threatDistanceToUs =
      Maff.min(With.geography.ourBases.map(_.heart.tileDistanceFast(threatOrigin)))
        .getOrElse(With.geography.home.tileDistanceFast(threatOrigin))
    val threatDistanceToEnemy =
      Maff.min(With.geography.enemyBases.map(_.heart.tileDistanceFast(centroid)))
        .getOrElse(With.scouting.mostBaselikeEnemyTile.tileDistanceFast(centroid))

    lazy val enemyNonTrollyThreats = With.units.enemy.count(u => u.is(MatchWarriors) && u.likelyStillThere && ! u.is(Terran.Vulture) && u.detected)
    if (With.enemies.exists( ! _.isZerg)
      && With.enemy.bases.size < 3
      && threatDistanceToUs < threatDistanceToEnemy
      && enemyNonTrollyThreats > 6) {
      vicinity = With.scouting.threatOrigin.center
      return
    }
    vicinity =
      // Horror proxies/Gas steals
      Maff.minBy(With.geography.ourBasesAndSettlements.flatMap(_.units.filter(u => u.isEnemy && u.unitClass.isBuilding).map(_.pixel)))(_.groundPixels(With.geography.home.center))
      // Remote proxies
      .orElse(
        if (With.geography.ourBases.size > 1 && With.frame > Minutes(10)()) None
        else Maff.minBy(With.units.enemy.view.filter(MatchProxied).map(_.pixel))(_.groundPixels(With.geography.home.center)))
      // Enemy base furthest from their army
      .orElse(
        Maff.maxBy(With.geography.enemyBases)(base => {
            val distance      = With.scouting.threatOrigin.center.pixelDistance(base.heart.center)
            val distanceLog   = 1 + Math.log(1 + distance)
            val defendersLog  = 1 + Math.log(1 + base.defenseValue)
            val output        = distanceLog / defendersLog
            output
          })
          .map(base => base.natural.filter(_.owner == base.owner).getOrElse(base))
          .map(base => Maff.minBy(base.units.filter(u => u.isEnemy && u.unitClass.isBuilding))(_.pixelDistanceCenter(base.townHallArea.center))
            .map(_.pixel)
            .getOrElse(base.townHallArea.center)))
      .orElse(if (enemyNonTrollyThreats > 0) Some(With.scouting.threatOrigin.center) else None)
      .getOrElse(With.scouting.mostBaselikeEnemyTile.center)
  }
}
