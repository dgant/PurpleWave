package Micro.Squads

import Mathematics.PurpleMath
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.CountMap

class QualityCounter {

  final protected val qualitiesEnemy = new CountMap[Quality]
  final protected val qualitiesPossessed = new CountMap[Quality]

  final protected var unitsNeeded = new CountMap[UnitMatcher]
  final protected var unitsPossessed = new CountMap[UnitMatcher]

  def setNeeds(needs: CountMap[UnitMatcher]): Unit = {
    unitsNeeded = needs
  }

  def countUnit(unit: UnitInfo): Unit = {
    if (unit.isFriendly) {
      Qualities.friendly.foreach(countUnitQuality(unit, _))
      unitsNeeded.keys.foreach(countUnitNeed(unit.friendly.get, _))
    } else {
      Qualities.enemy.foreach(countUnitQuality(unit, _))
    }
  }

  protected def countUnitQuality(unit: UnitInfo, quality: Quality): Unit = {
    if (unit.is(quality)) {
      // TODO: Scale by more appropriate value eg Wraith not as good as Siege Tank vs ground
      val value = unit.subjectiveValue
      (if (unit.isFriendly) qualitiesPossessed else qualitiesEnemy)(quality) += value.toInt
    }
  }

  protected def countUnitNeed(unit: FriendlyUnitInfo, matcher: UnitMatcher): Unit = {
    if (unit.is(matcher)) unitsPossessed(matcher) += 1
  }

  // Captures the idea that if we have *no* units which serve a role, we really want at least one (eg anti-air, detector, etc)
  // but as we pass the amount we need, the marginal value rapidly declines
  def scaleNeed(value: Double): Double = {
    PurpleMath.clamp(value, 0.01, 100)
  }

  def utility(unit: FriendlyUnitInfo): Double = {
    val utilitiesQualities = utilityQualities(unit)
    val utilitiesNeed = utilityNeed(unit)
    val output = (utilitiesQualities.map(_._3) ++ utilitiesNeed.map(_._2) :+ 0.01).max
    output
  }

  def utilityQualities(unit: FriendlyUnitInfo): Seq[(Quality, Quality, Double)] = {
    val friendlyQualities = Qualities.friendly.view.filter(_.accept(unit))
    val qualities = friendlyQualities.flatMap(friendlyQuality =>
      qualitiesEnemy.view.flatMap(enemyQuality =>
        enemyQuality._1.counteredBy.view
          .filter(_ == friendlyQuality)
          .map(counterQuality => {
            val baseValue = counterQuality.counterScaling
            val valueEnemy = enemyQuality._2
            val valueFriendly = qualitiesPossessed(counterQuality)
            val needMultiplier = scaleNeed(PurpleMath.nanToInfinity(valueEnemy.toDouble / valueFriendly))
            (friendlyQuality, enemyQuality._1, baseValue * needMultiplier)
        })))
    qualities
  }

  def utilityNeed(unit: FriendlyUnitInfo): Seq[(UnitMatcher, Double)] = {
    val needs = unitsNeeded.keys.view.filter(_.accept(unit)).map(need => {
      val valueNeeded = unitsNeeded(need)
      val valuePossessed = unitsPossessed(need)
      (need, scaleNeed(PurpleMath.nanToInfinity(valueNeeded.toDouble / valuePossessed)))
    }).toSeq
    needs
  }
}
