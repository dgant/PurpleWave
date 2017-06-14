package Micro.Formations

import Mathematics.Points.Pixel
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Formation {
  
  def concave(
    units       : Iterable[FriendlyUnitInfo],
    targetStart : Pixel,
    targetEnd   : Pixel,
    origin      : Pixel) {
    
    val formationSlots = new mutable.HashMap[UnitClass, ListBuffer[Pixel]]
    
    Concave
      .generate(units, targetStart, targetEnd, origin)
      .groupBy(_.unitClass)
      .foreach(pair => {
        if (!formationSlots.contains(pair._1)) {
          formationSlots.put(pair._1, new ListBuffer[Pixel])
        }
        pair._2.map(_.pixelAfter).foreach(pixel => formationSlots(pair._1).append(pixel))
      })
  
    val center = targetStart.midpoint(targetEnd)
  
    units
      .toArray
      .sortBy(_.pixelDistanceFast(center))
      .foreach(unit => {
        val groupSlots = formationSlots(unit.unitClass)
        val bestSlot = groupSlots.minBy(unit.pixelDistanceFast)
        unit.executionState.toForm = Some(bestSlot)
        groupSlots -= bestSlot
      })
  }
}
