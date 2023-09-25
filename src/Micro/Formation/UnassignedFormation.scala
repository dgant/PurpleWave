package Micro.Formation

import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Tactic.Squads.FriendlyUnitGroup
import Utilities.SomeIf

import scala.collection.mutable.ListBuffer

case class UnassignedFormation(style: FormationStyle, slots: Map[UnitClass, Iterable[Pixel]], group: FriendlyUnitGroup) {
  private def assignOutwardFromCentroid(unitClass: UnitClass): Map[FriendlyUnitInfo, Pixel] = {
    val classUnits = group.groupFriendlyOrderable.view.filter(unitClass)
    if (classUnits.isEmpty) return Map.empty
    val centroid = Maff.centroid(classUnits.map(_.pixel))
    val pixels = slots(unitClass).toSeq.sortBy(-_.pixelDistanceSquared(centroid))
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

  private def assignRadiallyFromCentroid(unitClass: UnitClass): Map[FriendlyUnitInfo, Pixel] = {
    val classUnits = group.groupFriendlyOrderable.view.filter(unitClass)
    if (classUnits.isEmpty) return Map.empty
    val mapping = Maff.mapFieldsRadially(classUnits.map(_.pixel), slots(unitClass).toSeq).toMap
    classUnits.view.flatMap(u => mapping.get(u.pixel).map(p => (u, p))).toMap
  }

  def outwardFromCentroid: Map[FriendlyUnitInfo, Pixel] = {
    if (slots.isEmpty) return Map.empty
    slots.keys.map(unitClass => assignOutwardFromCentroid(unitClass)).reduce(_ ++ _)
  }

  def radiallyFromCentroid: Map[FriendlyUnitInfo, Pixel] = {
    if (slots.isEmpty) return Map.empty
    slots.keys.map(unitClass => assignRadiallyFromCentroid(unitClass)).reduce(_ ++ _)
  }

  def sprayToward(to: Pixel): Map[FriendlyUnitInfo, Pixel] = {
    // Goal: Solve what https://www.gamasutra.com/view/feature/3314/coordinated_unit_movement.php?print=1
    // calls the "stacked canyon" problem.
    // In Brood War ALL army movement is a stacked canyon problem
    // because of the engine-powered tendency to force units into conga lines.
    val allSlots = slots.view.flatMap(_._2)
    val centroid = Maff.centroid(allSlots)
    // Rank slots by front-outsideness (a metric which may need some tuning)
    // Rank units by proximity to goal
    // Assign units to slot of equal rank
    val orderedSlots = slots.map(p => (p._1, p._2.toVector.sortBy(p => 5 * p.tile.groundTiles(to) - p.tile.groundTiles(centroid))))
    val orderedUnits = group
      .groupFriendlyOrderable
      .groupBy(_.unitClass)
      .map(p => (
        p._1,
        p._2.toVector.sortBy(_.pixelDistanceTravelling(to))))
    val output = orderedUnits
      .view
      .filter(p => orderedSlots.contains(p._1))
      .flatMap(p =>
        //The .take() is protection against array bounds issues I never investigated
        orderedSlots(p._1).indices.take(p._2.size).map(i =>
          (p._2(i),
          orderedSlots(p._1)(i))))
      .toMap
    output
  }
}
