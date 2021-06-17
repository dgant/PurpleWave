package Micro.Squads.Qualities

import Mathematics.Maff
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, CountMap}

class QualityCounter {

  private val qualitiesEnemy = new CountMap[Quality]
  private val qualitiesFriendly = new CountMap[Quality]
  private val unitsPossessed = new CountMap[UnitMatcher]

  def clear(): Unit = {
    qualitiesEnemy.clear()
    qualitiesFriendly.clear()
    unitsPossessed.clear()
  }

  def countUnit(unit: UnitInfo): Unit = {
    (if (unit.isFriendly) Qualities.friendly else Qualities.enemy).foreach(countUnitQuality(unit, _))
  }

  private def countUnitQuality(unit: UnitInfo, quality: Quality): Unit = {
    (if (unit.isFriendly) qualitiesFriendly else qualitiesEnemy)(quality) += unitValue(unit, quality)
  }

  private def unitValue(unit: UnitInfo, quality: Quality): Int = {
    if (unit.is(quality)) unit.subjectiveValue.toInt else 0
  }

  private def countUnitNeed(unit: FriendlyUnitInfo, matcher: UnitMatcher): Unit = {
    unitsPossessed(matcher) += Maff.fromBoolean(unit.is(matcher))
  }

  // Captures the idea that if we have *no* units which serve a role, we really want at least one (eg anti-air, detector, etc)
  // but as we pass the amount we need, the marginal value rapidly declines
  def scaleNeed(value: Double): Double = Maff.clamp(value, 0.01, 100)

  def utility(unit: FriendlyUnitInfo): Double = ByOption.max(utilityQualities(unit).view.map(_._3)).getOrElse(0.0)

  def utilityQualities(unit: FriendlyUnitInfo): Seq[(Quality, Quality, Double)] =
    Qualities.friendly
      .view
      .filter(_(unit))
      .flatMap(friendlyQuality =>
        qualitiesEnemy
          .view
          .flatMap(enemyQuality =>
            enemyQuality._1.counteredBy
              .view
              .filter(_ == friendlyQuality)
              .map(unused => {
                val valueRequired = enemyQuality._2
                val valuePossessed = qualitiesFriendly(friendlyQuality) * friendlyQuality.counterScaling
                // We have thoroughly surpassed the required value: 0.0
                // We are already at exact match value: 1.0
                // We have no existing value: 2.0
                val score = 1 + Maff.clamp((valueRequired - valuePossessed) / valueRequired, -1, 1)
                (friendlyQuality, enemyQuality._1, score)
            })))
}
