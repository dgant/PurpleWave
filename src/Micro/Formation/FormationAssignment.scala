package Micro.Formation

import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ListBuffer

object FormationAssignment {

  def outwardFromCentroid(style: FormationStyle, spots: Map[UnitClass, Iterable[Pixel]], units: Iterable[UnitInfo]): Formation = {
    if (spots.isEmpty) return FormationEmpty
    def assignUnitsOutward(unitClass: UnitClass, units: Iterable[UnitInfo]): Map[UnitInfo, Pixel] = {
      if (units.isEmpty) return Map.empty
      val centroid = PurpleMath.centroid(units.map(_.pixel))
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
    new Formation(
      style,
      spots.keys
        .map(unitClass => assignUnitsOutward(unitClass, units.filter(unitClass)))
        .reduce(_ ++ _))
  }
}