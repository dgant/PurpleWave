package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Utilities.UnitFilters.{IsBuilding, IsMobileDetector, IsProxied, IsWarrior}
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Time.Minutes

class SquadAttack extends Squad {
  override def toString: String = f"Atk ${vicinity.base.map(_.name).getOrElse(vicinity.zone.name).take(4)}"

  override def launch(): Unit = { /* This squad is given its recruits externally */ }

  override def run(): Unit = {
    if (units.isEmpty) return
    vicinity = getVicinity
    SquadAutomation.targetFormAndSend(this)
  }

  private def isNonTrolly(u: UnitInfo): Boolean = u.likelyStillThere && ! u.flying && IsWarrior(u) && ! u.isAny(Terran.Vulture, Zerg.Zergling) && ( ! u.effectivelyCloaked || With.units.existsOurs(IsMobileDetector) || With.units.existsOurs(Terran.Comsat))
  protected def getVicinity: Pixel = {
    lazy val weHaveDetector = units.exists(_.unitClass.isDetector) || With.units.existsOurs(Terran.Comsat)
    lazy val enemyNonTrollyThreats = With.units.enemy.count(isNonTrolly)
    lazy val airValue = units.view.filter(_.flying).map(_.subjectiveValue).sum
    lazy val totalValue = units.view.map(_.subjectiveValue).sum
    lazy val baseScores = With.geography.enemyBases.map(b => {
      val distanceThreat = With.scouting.enemyThreatOrigin.walkableTile.groundPixels(b.heart.center)
      val distanceArmy   = keyDistanceTo(b.heart.center)
      val distanceHome    = With.geography.home.groundPixels(b.heart)
      val accessibility  = if (b.zone.island) Math.pow(Maff.nanToZero(airValue / totalValue), 2) else 1.0
      (b, accessibility * (3 * distanceThreat - distanceArmy - distanceHome))
    }).toMap
    val aggressiveDivisions = With.battles.divisions
      .filter(_.enemies.exists(isNonTrolly))
      .map(d => (d, d.attackCentroidGround))
      .map(d => (d._1, d._2, With.scouting.proximity(d._2)))
    val aggressivestDivision = Maff.maxBy(aggressiveDivisions)(_._3)
    if (aggressivestDivision.exists(_._3 > 0.35)) {
      return aggressivestDivision.get._2
    }

    val targets = Maff.orElse(
      // Horror proxies/Gas steals
      Maff.minBy(With.geography.ourBasesAndSettlements.flatMap(_.enemies.filter(IsBuilding).map(_.pixel)))(_.groundPixels(With.geography.home.center)),
      // Remote proxies
      if (With.geography.ourBases.size > 1 && With.frame > Minutes(10)()) None else Maff.minBy(With.units.enemy.view.filter(IsProxied).map(_.pixel))(_.groundPixels(With.geography.home.center)),
      // Highest scoring enemy base
      Maff.maxBy(baseScores)(_._2).map(b => Maff.minBy(b._1.enemies.filter(_.unitClass.isBuilding).map(_.pixel))(keyDistanceTo).getOrElse(b._1.townHallArea.center)),
      // Threat option, if there's an army to pursue
      aggressivestDivision.map(_._2),
      Some(With.scouting.enemyThreatOrigin.center))
    targets.headOption.getOrElse(With.scouting.enemyHome.center)
  }
}
