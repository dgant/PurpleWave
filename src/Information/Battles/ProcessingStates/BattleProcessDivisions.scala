package Information.Battles.ProcessingStates

import Information.Battles.BattleClassificationFilters
import Information.Battles.Types.Division
import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo


class BattleProcessDivisions extends BattleProcessState {
  override def step(): Unit = {

    val battles = With.battles.nextBattlesLocal

    var zones = (With.geography.enemyBases ++ With.geography.ourBasesAndSettlements).map(_.zone).distinct
    if (zones.isEmpty) {
      zones = Vector(With.geography.home.zone, With.scouting.mostBaselikeEnemyTile.zone).distinct
    }

    val salientBaseGroups = Seq(
      With.geography.enemyBases,
      With.geography.ourBasesAndSettlements,
      With.geography.neutralBases)

    val zoneBattles = battles.groupBy(battle => zones.minBy(z =>
      if (battle.enemy.units.forall(_.flying))
        battle.enemy.centroidAir.pixelDistance(z.heart.center)
      else
        battle.enemy.centroidGround.groundPixels(z.heart)))

    // Associate each group of enemies with the nearest base
    val baseRelevancePixels = 32 * 32
    val enemiesAll = With.units.enemy.view.filter(BattleClassificationFilters.isEligibleLocal)
    val enemiesEmbattled = battles.view.map(_.enemy.units)
    val enemiesUnbattled = (enemiesAll.toSet -- battles.view.flatMap(_.enemy.units.view.flatMap(_.foreign)))
      .groupBy(e => e.base.getOrElse(With.geography.bases.minBy(distance(e, _)))).values
    var nextDivisions = (enemiesEmbattled ++ enemiesUnbattled)
      .map(enemies =>
        Division(
          enemies,
          enemies
            .view
            .flatMap(enemy =>
              salientBaseGroups
                .flatMap(bases =>
                  Maff.minBy(bases
                    .map(base => (base, distance(enemy, base)))
                    .filter(p => p._2 < baseRelevancePixels || enemy.base.contains(p._1)))(_._2)
                    .map(_._1)))
            .toSet))
      .toVector

    // Merge divisions with intersecting bases
    var mergedThisTime = false
    do {
      val lastDivisions = nextDivisions
      val toMerge = lastDivisions.view.flatMap(_.bases)
        .map(b => (b, lastDivisions.filter(_.bases.contains(b))))
        .find(_._2.size > 1)
        .map(_._2)
      mergedThisTime = toMerge.isDefined
      if (mergedThisTime) {
        nextDivisions = lastDivisions.filterNot(toMerge.get.contains) :+ toMerge.get.reduce(_.merge(_))
      }
    } while (mergedThisTime)

    With.battles.nextDivisions = nextDivisions

    transitionTo(new BattleProcessSimulate)
  }

  private def distance(unit: UnitInfo, base: Base): Double = unit.pixelDistanceTravelling(base.heart)
}
