package Micro.Formation

import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Tactic.Squads.FriendlyUnitGroup
import Utilities.UnitFilters.IsTank
import Utilities.{?, SomeIf}

import scala.collection.mutable.ListBuffer

case class UnassignedFormation(style: FormationStyle, slots: Map[UnitClass, Iterable[Pixel]], group: FriendlyUnitGroup) {

  private def assignOutwardFromCentroid(unitClass: UnitClass): Map[FriendlyUnitInfo, Pixel] = {
    val classUnits = group.groupFriendlyOrderable.view.filter(?(unitClass.isTank, IsTank, unitClass))

    if (classUnits.isEmpty) return Map.empty

    val centroid  = Maff.centroid(classUnits.map(_.pixel))
    val pixels    = slots(unitClass).toSeq.sortBy( - _.pixelDistanceSquared(centroid) )
    val unitsLeft = new ListBuffer[FriendlyUnitInfo]

    unitsLeft ++= classUnits
    pixels.flatMap(pixel =>
      SomeIf(
        unitsLeft.nonEmpty, {
          val unit = unitsLeft.minBy(_.pixelDistanceSquared(pixel))
          unitsLeft -= unit
          (unit, pixel)
        })
    ).toMap
  }

  def outwardFromCentroid: Map[FriendlyUnitInfo, Pixel] = {
    if (slots.isEmpty) return Map.empty
    slots.keys.map(unitClass => assignOutwardFromCentroid(unitClass)).reduce(_ ++ _)
  }
}
