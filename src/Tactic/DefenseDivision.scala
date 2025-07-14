package Tactic

import Information.Battles.Types.{Division, DivisionRadius}
import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import Tactic.Squads.UnitGroup
import Utilities.UnitFilters.IsWorker

class DefenseDivision(val division: Division) extends UnitGroup {

  val enemyDistances: Vector[(UnitInfo, Double)] = division
    .enemies
    .filter(defenseDanger(_) > 0)
    .map(e => (e, defenseDistance(e)))
    .toVector

  // Our approach to choosing relevant enemies is flawed because it can cut off portions of a cluster
  val       enemiesInner : Vector[UnitInfo] = enemyDistances.view.filter(_._2 < DivisionRadius.inner).map(_._1).toVector
  lazy val  enemiesOuter : Vector[UnitInfo] = enemyDistances.view.filter(_._2 < DivisionRadius.outer).map(_._1).toVector

  val needsDefense: Boolean = enemiesInner.view.map(defenseDanger).sum >= 1.0

  lazy val base: Base =
    GetDefenseBase(division.bases
      .toVector
      .sortBy( - _.economicValue())
      .sortBy( ! _.isEnemy)
      .sortBy( ! _.isOurs)
      .minBy( ! _.plannedExpoRecently)) // TODO: Base defense logic needs to handle case where OTHER bases need scouring and not concave in just one

  override def groupUnits: Seq[UnitInfo] = enemiesInner

  private def defenseDistance(e: UnitInfo): Double = {
    Maff.orElse(With.geography.ourBases.map(_.heart), Seq(With.geography.home)).map(e.pixelDistanceTravelling).min
  }

  private def defenseDanger(e: UnitInfo): Double = {
    if (IsWorker(e))
      0.35
    else if (Protoss.Observer(e) && e.matchups.groupVs.mobileDetectors.isEmpty)
      0.0
    else if (e.flying && (Zerg.Overlord(e) || e.unitClass.isFlyingBuilding) && ! e.matchups.groupVs.attacksAir)
      0.0
    else if (e.unitClass.attacksOrCastsOrDetectsOrTransports)
      1.0
    else
      0.0
  }
}