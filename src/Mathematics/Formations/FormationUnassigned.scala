package Mathematics.Formations

import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ListBuffer

class FormationUnassigned(var spots: Map[UnitClass, Iterable[Pixel]]) {

  def assign(units: Iterable[UnitInfo]): FormationAssigned = {
    new FormationAssigned(
      if (spots.isEmpty) Map.empty else
      spots.keys
        .map(unitClass => assignClass(unitClass, units.filter(_.is(unitClass))))
        .reduce(_ ++ _))
  }

  def assignClass(unitClass: UnitClass, units: Iterable[UnitInfo]): Map[UnitInfo, Pixel] = {
    if (units.isEmpty) return Map.empty
    val centroid = PurpleMath.centroid(units.map(_.pixelCenter))
    val pixels = spots(unitClass).toSeq.sortBy(-_.pixelDistanceSquared(centroid))
    val unitsLeft = new ListBuffer[UnitInfo]
    unitsLeft ++= units
    pixels.flatMap(pixel =>
      if (unitsLeft.nonEmpty) {
        val unit = unitsLeft.minBy(_.pixelDistanceSquared(pixel))
        unitsLeft -= unit
        Some((unit, pixel))
      } else None
    ).toMap
  }
}