package Mathematics.Formations.Designers

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Formations.{FormationAssigned, FormationSlot, FormationUnassigned}
import Mathematics.Points.{Pixel, Point}
import Mathematics.Shapes.Spiral
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

import scala.collection.mutable

class FormationZoneNew(zone: Zone, enemies: Seq[UnitInfo]) extends FormationDesigner {

  def edgeDistance(pixel: Pixel): Double = zone.exitDistanceGrid.get(pixel.tileIncluding)

  override def form(units: Seq[FriendlyUnitInfo]): FormationAssigned = {
    if (units.isEmpty) return new FormationAssigned(Map.empty)

    val occupied  = With.grids.disposableBoolean1()
    val slots     = units.map(new FormationSlot(_))
    val start     = zone.exit.map(_.pixelCenter).getOrElse(zone.centroid.pixelCenter)
    val startTile = start.tileIncluding
    val end       = zone.exit.map(_.pixelTowards(zone)).getOrElse(zone.centroid.pixelCenter)

    val enemyRangePixelsMin : Int = ByOption.min(enemies.view.map(_.effectiveRangePixels.toInt)).getOrElse(0)
    val enemyRangePixelsMax : Int = ByOption.max(enemies.view.map(_.effectiveRangePixels.toInt)).getOrElse(0)
    val meleeSizePixels     : Int = Math.max(16, slots.map(s => if (s.idealPixels > 32) 0 else s.unitClass.radialHypotenuse.toInt).max)
    val meleeRowWidthUnits  : Int = Math.max(1, zone.exit.map(_.radiusPixels.toInt).getOrElse(0) / meleeSizePixels)

    val meleeSlots    = new mutable.ArrayBuffer[(UnitClass, Pixel)]
    val arcSlots      = new mutable.ArrayBuffer[(UnitClass, Pixel)]

    val nullPoint   = Point(1000, 1000)
    val nullPoints  = Vector(nullPoint)
    val nullValue   = Int.MaxValue - 1
    slots.sortBy(_.idealPixels).foreach(slot => {
      val idealPixels = slot.idealPixels.toInt
      val idealTiles = (idealPixels + 16) / 32
      if (enemyRangePixelsMin < 32 && zone.exit.isDefined && idealPixels <= Math.max(32, enemyRangePixelsMin)) {
        // Against enemy melee units, place melee units directly into the exit
        val nextPixel = start
          .project(
            // Point towards the left/right side of the choke
            zone.exit.get.sidePixels(meleeSlots.size % 2),
            // If odd-sized row: First unit goes in the middle; otherwise offset by half a melee radius
            (if (meleeRowWidthUnits % 2 == 0) meleeSizePixels / 2 else 0)
            // Project unit towards the current side
            +  meleeSizePixels * (meleeSlots.size / 2) % meleeRowWidthUnits) +
          // Fill in rows from front to back
          Pixel(0, 0).project(end, meleeSizePixels * (meleeSlots.size / meleeRowWidthUnits))

        meleeSlots += ((slot.unitClass, nextPixel))
        occupied.set(nextPixel.tileIncluding, true)
      }
      else {
        // Apply arc positioning
        // Find the best point for this unit
        val point = (Spiral.points(11) ++ nullPoints).minBy(p => {
          if (p == nullPoint) {
            nullValue
          } else {
            val tile = startTile.add(p)
            if (tile.valid && zone.tileGrid.get(tile) && ! occupied.get(tile) && With.grids.walkable.get(tile)) {
              val exitDistance = zone.exitDistanceGrid.get(tile)
              val distanceIntoEnemyRangeNow   = 1 + With.grids.enemyRange.get(tile) - With.grids.enemyRange.addedRange
              val distanceIntoEnemyRangeExit  = Math.max(0, enemyRangePixelsMax / 32 - exitDistance)
              val distanceOutOfOurRange       = Math.max(0, exitDistance - idealTiles)
              5 * distanceIntoEnemyRangeNow + 4 * distanceIntoEnemyRangeExit + 3 * distanceOutOfOurRange
            } else {
              Int.MaxValue
            }
          }})
        if (point != nullPoint) {
          // Assign the unit
          val tile = startTile.add(point)
          occupied.set(tile, true)
          arcSlots += ((slot.unitClass, tile.pixelCenter))
        }
      }
    })
    new FormationUnassigned(
      (meleeSlots.view ++ arcSlots.view)
        .groupBy(_._1)
        .mapValues(_.map(_._2)))
      .assign(units)
  }

}
