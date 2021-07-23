package Micro.Formation

import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo
import Tactics.Squads.UnitGroup

import scala.collection.mutable.ListBuffer

case class UnassignedFormation(style: FormationStyle, slots: Map[UnitClass, Iterable[Pixel]], group: UnitGroup) {
  private def assignOutwardFromCentroid(unitClass: UnitClass): Map[UnitInfo, Pixel] = {
    val classUnits = group.groupOrderable.view.filter(unitClass)
    if (classUnits.isEmpty) return Map.empty
    val centroid = Maff.centroid(classUnits.map(_.pixel))
    val pixels = slots(unitClass).toSeq.sortBy(-_.pixelDistanceSquared(centroid))
    val unitsLeft = new ListBuffer[UnitInfo]
    unitsLeft ++= classUnits
    pixels.flatMap(pixel =>
      if (unitsLeft.nonEmpty) {
        val unit = unitsLeft.minBy(_.pixelDistanceSquared(pixel))
        unitsLeft -= unit
        Some((unit, pixel))
      } else None
    ).toMap
  }

  def outwardFromCentroid: Formation = {
    if (slots.isEmpty) return FormationEmpty
    new Formation(
      style,
      slots
        .keys
        .map(unitClass => assignOutwardFromCentroid(unitClass))
        .reduce(_ ++ _))
  }

  def sprayToward(to: Pixel): Formation = {
    if (slots.isEmpty) return FormationEmpty
    // Goal: Solve what https://www.gamasutra.com/view/feature/3314/coordinated_unit_movement.php?print=1
    // calls the "stacked canyon" problem.
    // In Brood War ALL army movement is a stacked canyon problem
    // because of the engine-powered tendency to force units into conga lines.
    val allSlots = slots.view.flatMap(_._2)
    val centroid = Maff.centroid(allSlots)
    // Rank slots by front-outsideness (a metric which may need some tuning)
    // Rank units by proximity to goal
    // Assign units to slot of equal rank
    val orderedSlots = slots.map(p => (p._1, p._2.toVector.sortBy(p => 5 * p.tile.tileDistanceGroundManhattan(to) - p.tile.tileDistanceGroundManhattan(centroid))))
    val orderedUnits = group
      .groupOrderable
      .groupBy(_.unitClass)
      .map(p => (
        p._1,
        p._2.toVector.sortBy(_.pixelDistanceTravelling(to))))
    val output = new Formation(
      style,
      orderedUnits.flatMap(p =>
        //The .take() is protection against array bounds issues I never investigated
        orderedSlots(p._1).indices.take(p._2.size).map(i =>
          (p._2(i),
          orderedSlots(p._1)(i)))))
    output
  }
}
