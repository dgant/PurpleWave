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
    ?(unit.isFriendly, qualitiesFriendly, qualitiesEnemy)(quality) += ?(quality(unit), unit.subjectiveValue.toInt, 0)
  }

  def utility(unit: FriendlyUnitInfo): Double = {
    var max = 0.0
    val qfi = qualitiesFriendly.iterator
    while (qfi.hasNext) {
      val (qf, qfc) = qfi.next()
      val qei = qualitiesEnemy.iterator
      while (qei.hasNext) {
        val (qe, qec) = qei.next()
        var i = 0
        while (i <  qe.counteredBy.length) {
          if (qf == qe.counteredBy(i)) {
            // We have thoroughly surpassed the required value: 0.0
            // We are already at exact match value: 1.0
            // We have no existing value: 2.0
            max = Math.max(max, 1 + Maff.nanToZero(Maff.clamp((qec - qfc * qe.counterScaling) / qec, -1, 1)))
          }
          i += 1
        }
      }
    }
    max
  }
}
