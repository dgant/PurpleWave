package Micro.Squads

import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, CountMap}

class QualityCounter {

  final protected val enemyQualities = new CountMap[Quality]
  final protected val friendlyQualities = new CountMap[Quality]

  def countUnit(unit: UnitInfo): Unit = {
    if (unit.isFriendly) {
      Qualities.friendly.foreach(countUnit(unit, _))
    } else {
      Qualities.enemy.foreach(countUnit(unit, _))
    }
  }

  protected def countUnit(unit: UnitInfo, quality: Quality): Unit = {
    if (unit.is(quality)) {
      // TODO: Scale by more appropriate value eg Wraith not as good as Siege Tank vs ground
      val value = unit.subjectiveValue
      (if (unit.isFriendly) friendlyQualities else enemyQualities)(quality) += value.toInt
    }
  }

  def utility(unit: FriendlyUnitInfo): Double = {
    unit.subjectiveValue * ByOption.max(
      Qualities.friendly.view.filter(_.accept(unit)).flatMap(friendlyQuality =>
        enemyQualities.view.flatMap(enemyQuality =>
          enemyQuality._1.counteredBy.view.map(counterQuality => {
            val baseValue = if (counterQuality == friendlyQuality) { counterQuality.counterScaling } else 0.0
            val valueEnemy = enemyQualities(counterQuality)
            val valueFriendly = friendlyQualities(counterQuality)
            val needMultiplier = PurpleMath.clamp(PurpleMath.nanToInfinity(2 * valueEnemy / valueFriendly), 0.01, 100.0)
            baseValue * needMultiplier
          })))).getOrElse(0.0)
  }
}
