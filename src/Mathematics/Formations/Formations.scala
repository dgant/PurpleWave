package Mathematics.Formations

import Mathematics.Points.Pixel
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Formations {
  
  def concave(
    units       : Iterable[FriendlyUnitInfo],
    targetStart : Pixel,
    targetEnd   : Pixel,
    origin      : Pixel)
      : FormationAssigned = {
    
    val formationSlots = new mutable.HashMap[UnitClass, ListBuffer[Pixel]]
    
    Concave
      .static(units, targetStart, targetEnd, origin)
      .groupBy(_.unitClass)
      .foreach(pair => {
        if (!formationSlots.contains(pair._1)) {
          formationSlots.put(pair._1, new ListBuffer[Pixel])
        }
        pair._2.map(_.pixelAfter).foreach(pixel => formationSlots(pair._1).append(pixel))
      })
  
    val center = targetStart.midpoint(targetEnd)

    new FormationUnassigned(formationSlots.toMap).assign(units)
  }
}
