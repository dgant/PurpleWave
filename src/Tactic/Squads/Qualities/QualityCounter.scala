package Tactic.Squads.Qualities

import Mathematics.Maff
import Utilities.UnitFilters.UnitFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{?, CountMap}

final class QualityCounter {

  val qualitiesEnemy          : CountMap[Quality]     = new CountMap[Quality]
  val qualitiesFriendly       : CountMap[Quality]     = new CountMap[Quality]
  var qualitiesUseful         : Set[Quality]          = Set.empty
  private val unitsPossessed  : CountMap[UnitFilter]  = new CountMap[UnitFilter]

  def clear(): Unit = {
    qualitiesEnemy.clear()
    qualitiesFriendly.clear()
    qualitiesUseful = Set.empty
    unitsPossessed.clear()
  }

  @inline def countUnit(unit: UnitInfo): Unit = {
    ?(unit.isFriendly, Qualities.friendly, Qualities.enemy)
      .filter(q => unit.isEnemy || qualitiesUseful.contains(q))
      .foreach(countUnitQuality(unit, _))
  }

  @inline private def countUnitQuality(unit: UnitInfo, quality: Quality): Unit = {
    ?(unit.isFriendly, qualitiesFriendly, qualitiesEnemy)(quality) += ?(quality(unit), unit.subjectiveValue.toInt, 0)
  }

  /**
    * Performance-optimizing step:
    * Make the keyset of qualitiesFriendly match the qualities we actually care about
    */
  def alignFriendlyQualities(): Unit = {
    qualitiesFriendly.clear()
    qualitiesUseful = qualitiesEnemy.iterator.filter(_._2 > 0).flatMap(_._1.counteredBy).toSet
    qualitiesUseful.foreach(qualitiesFriendly(_) = 0)
  }

  @inline def utility(unit: FriendlyUnitInfo): Double = {
    var max = 0.0
    val qfi = qualitiesFriendly.iterator
    while (qfi.hasNext) {
      val (qf, qfc) = qfi.next()
      if (qf(unit)) {
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
    }
    max
  }
}
