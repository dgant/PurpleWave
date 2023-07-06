package Tactic.Squads.Qualities

import Mathematics.Maff
import Utilities.UnitFilters.UnitFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{?, CountMap}

class QualityCounter {

  val qualitiesEnemy          = new CountMap[Quality]
  val qualitiesFriendly       = new CountMap[Quality]
  private val unitsPossessed  = new CountMap[UnitFilter]

  def clear(): Unit = {
    qualitiesEnemy.clear()
    qualitiesFriendly.clear()
    unitsPossessed.clear()
  }

  def countUnit(unit: UnitInfo): Unit = {
    ?(unit.isFriendly, Qualities.friendly, Qualities.enemy).foreach(countUnitQuality(unit, _))
  }

  private def countUnitQuality(unit: UnitInfo, quality: Quality): Unit = {
    ?(unit.isFriendly, qualitiesFriendly, qualitiesEnemy)(quality) += unitValue(unit, quality)
  }

  private def unitValue(unit: UnitInfo, quality: Quality): Int = {
    ?(quality(unit), unit.subjectiveValue.toInt, 0)
  }

  private def countUnitNeed(unit: FriendlyUnitInfo, matcher: UnitFilter): Unit = {
    unitsPossessed(matcher) += Maff.fromBoolean(matcher(unit))
  }

  // Captures the idea that if we have *no* units which serve a role, we really want at least one (eg anti-air, detector, etc)
  // but as we pass the amount we need, the marginal value rapidly declines
  def scaleNeed(value: Double): Double = Maff.clamp(value, 0.01, 100)

  def utility(unit: FriendlyUnitInfo): Double = Maff.max(utilityQualities(unit).view.map(_._3)).getOrElse(0.0)

  def utilityQualities(unit: FriendlyUnitInfo): Seq[(Quality, Quality, Double)] =
    Qualities.friendly
      .view
      .filter(_(unit))
      .flatMap(friendlyQuality =>
        qualitiesEnemy
          .view
          .filter(_._2 > 0)
          .flatMap(enemyQuality =>
            enemyQuality._1.counteredBy
              .view
              .filter(friendlyQuality==)
              .map(unused => {
                val valueRequired = enemyQuality._2
                val valuePossessed = qualitiesFriendly(friendlyQuality) * friendlyQuality.counterScaling
                // We have thoroughly surpassed the required value: 0.0
                // We are already at exact match value: 1.0
                // We have no existing value: 2.0
                val score = 1 + Maff.nanToZero(Maff.clamp((valueRequired - valuePossessed) / valueRequired, -1, 1))
                (friendlyQuality, enemyQuality._1, score)
            })))
}
