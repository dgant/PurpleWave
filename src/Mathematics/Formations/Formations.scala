package Mathematics.Formations

import Mathematics.Points.Pixel
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable.ListBuffer

object Formations {
  
  def concave(
    units       : Iterable[FriendlyUnitInfo],
    targetStart : Pixel,
    targetEnd   : Pixel,
    origin      : Pixel)
      : FormationAssigned = {

    val formationSlots = units
      .map(_.unitClass)
      .toSet[UnitClass]
      .map(uc => (uc, new ListBuffer[Pixel]))
      .toMap

    Concave
      .static(units, targetStart, targetEnd, origin)
      .groupBy(_.unitClass)
      .foreach(pair =>  pair._2.map(_.pixelAfter).foreach(pixel => formationSlots(pair._1).append(pixel)))

    new FormationUnassigned(formationSlots).assign(units)
  }
}
