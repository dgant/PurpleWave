package Tactics.Squads

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Planning.UnitMatchers.{MatchProxied, MatchWarriors}
import ProxyBwapi.Races.Terran
import Utilities.Time.Minutes

class SquadAttack extends Squad {
  override def toString: String = f"Atk ${vicinity.base.map(_.name).getOrElse(vicinity.zone.name).take(4)}"

  override def launch(): Unit = { /* This squad is given its recruits externally */ }

  override def run() {
    if (units.isEmpty) return
    vicinity = getVicinity
    SquadAutomation.targetFormAndSend(this)
  }

  protected def getVicinity: Pixel = {
    lazy val enemyNonTrollyThreats = With.units.enemy.count(u => MatchWarriors(u) && u.likelyStillThere && ! Terran.Vulture(u) && u.detected)
    lazy val airValue = units.view.filter(_.flying).map(_.subjectiveValue).sum
    lazy val totalValue = units.view.map(_.subjectiveValue).sum
    lazy val baseScores = With.geography.enemyBases.map(b => {
      val distanceThreat = With.scouting.threatOrigin.walkableTile.groundPixels(b.heart.center)
      val distanceArmy = keyDistanceTo(b.heart.center)
      val accessibility = if (b.zone.island) Math.pow(Maff.nanToZero(airValue / totalValue), 2) else 1.0
      (b, accessibility * (2 * distanceThreat - distanceArmy))
    }).toMap
    if (With.enemies.exists( ! _.isZerg)
      && With.enemy.bases.size < 3
      && With.scouting.enemyProgress > 0.4
      && enemyNonTrollyThreats > 6) {
      return With.scouting.threatOrigin.center
    }
    // Horror proxies/Gas steals
    Maff.orElse(
      Maff.minBy(With.geography.ourBasesAndSettlements.flatMap(_.units.filter(u => u.isEnemy && u.unitClass.isBuilding).map(_.pixel)))(_.groundPixels(With.geography.home.center)),
      // Remote proxies
      if (With.geography.ourBases.size > 1 && With.frame > Minutes(10)()) None else Maff.minBy(With.units.enemy.view.filter(MatchProxied).map(_.pixel))(_.groundPixels(With.geography.home.center)),
      // Highest scoring enemy base
      Maff.maxBy(baseScores)(_._2).map(b => Maff.minBy(b._1.units.view.filter(_.isEnemy).filter(_.unitClass.isBuilding).map(_.pixel))(keyDistanceTo).getOrElse(b._1.townHallArea.center)),
      // Threat option, if there's an army to pursue
      Some(With.scouting.threatOrigin.center).filter(unused => enemyNonTrollyThreats > 0)).headOption
      .getOrElse(With.scouting.mostBaselikeEnemyTile.center)
  }
}
