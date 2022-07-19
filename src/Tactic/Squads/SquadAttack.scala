package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Utilities.UnitFilters.{IsBuilding, IsProxied, IsWarrior}
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
    lazy val enemyNonTrollyThreats = With.units.enemy.count(u => IsWarrior(u) && u.likelyStillThere && ! Terran.Vulture(u) && u.detected)
    lazy val airValue = units.view.filter(_.flying).map(_.subjectiveValue).sum
    lazy val totalValue = units.view.map(_.subjectiveValue).sum
    lazy val baseScores = With.geography.enemyBases.map(b => {
      val distanceThreat = With.scouting.enemyThreatOrigin.walkableTile.groundPixels(b.heart.center)
      val distanceArmy = keyDistanceTo(b.heart.center)
      val accessibility = if (b.zone.island) Math.pow(Maff.nanToZero(airValue / totalValue), 2) else 1.0
      (b, accessibility * (2 * distanceThreat - distanceArmy))
    }).toMap
    if (With.enemies.exists( ! _.isZerg)
      && With.enemy.bases.size < 3
      && With.scouting.enemyProximity > 0.4
      && enemyNonTrollyThreats > 6) {
      return With.scouting.enemyThreatOrigin.center
    }
    // Horror proxies/Gas steals
    Maff.orElse(
      Maff.minBy(With.geography.ourBasesAndSettlements.flatMap(_.enemies.filter(IsBuilding).map(_.pixel)))(_.groundPixels(With.geography.home.center)),
      // Remote proxies
      if (With.geography.ourBases.size > 1 && With.frame > Minutes(10)()) None else Maff.minBy(With.units.enemy.view.filter(IsProxied).map(_.pixel))(_.groundPixels(With.geography.home.center)),
      // Highest scoring enemy base
      Maff.maxBy(baseScores)(_._2).map(b => Maff.minBy(b._1.enemies.filter(_.unitClass.isBuilding).map(_.pixel))(keyDistanceTo).getOrElse(b._1.townHallArea.center)),
      // Threat option, if there's an army to pursue
      Some(With.scouting.enemyThreatOrigin.center).filter(unused => enemyNonTrollyThreats > 0)).headOption
      .getOrElse(With.scouting.enemyHome.center)
  }
}
