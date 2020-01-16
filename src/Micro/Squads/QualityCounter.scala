package Micro.Squads

import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.CountMap

class QualityCounter {

  final protected val enemyByQuality  = new CountMap[Quality]
  final protected val friendlyByQuality = new CountMap[Quality]

  def countUnit(unit: UnitInfo): Unit = {
    if (unit.isFriendly) {
      Array(Qualities.roles, Qualities.answers).foreach(_.foreach(countUnit(unit, _)))
    } else {
      Qualities.threats.foreach(countUnit(unit, _))
    }
  }

  protected def countUnit(unit: UnitInfo, quality: Quality): Unit = {
    if (unit.is(quality)) {
      // TODO: Scale by more appropriate value eg Wraith not as good as Siege Tank vs ground
      val value = unit.subjectiveValue
      (if (unit.isFriendly) friendlyByQuality else enemyByQuality)(quality) += value.toInt
    }
  }
}
