package Mathematics.Formations.Designers

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Formations.{FormationAssigned, FormationSlot, FormationUnassigned}
import Mathematics.Points.{Pixel, Point}
import Mathematics.Shapes.Spiral
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

import scala.collection.mutable

class FormationZone(zone: Zone, enemies: Seq[UnitInfo]) extends FormationDesigner {

  override def form(units: Seq[FriendlyUnitInfo]): FormationAssigned = {
    if (units.isEmpty) return new FormationAssigned(Map.empty)

    val occupied  = With.grids.disposableBoolean1()
    val slots     = units.map(new FormationSlot(_))
    val start     = zone.exitNow.map(_.pixelCenter).getOrElse(zone.centroid.pixelCenter)
    val startTile = start.tileIncluding
    val end       = start.project(zone.exitNow.map(_.pixelTowards(zone)).getOrElse(zone.centroid.pixelCenter), 300)

    val allEnemies = (enemies.view ++ units.flatMap(_.battle).distinct.map(_.enemy)).distinct
    val enemyRangePixelsMin   : Int = ByOption.min(enemies.view.map(_.effectiveRangePixels.toInt)).getOrElse(32 * 5)
    val enemyRangePixelsMax   : Int = ByOption.max(enemies.view.map(_.effectiveRangePixels.toInt)).getOrElse(32 * 5)
    val meleeUnitDiameter     : Int = Math.max(16, slots.map(s => if (s.idealPixels > 32) 0 else s.unitClass.dimensionMax.toInt).max)
    val meleeChokeWidthUnits  : Int = Math.max(1, 2 * zone.exitNow.map(_.radiusPixels.toInt).getOrElse(0) / meleeUnitDiameter)

    val meleeSlots = new mutable.ArrayBuffer[(UnitClass, Pixel)]
    val arcSlots   = new mutable.ArrayBuffer[(UnitClass, Pixel)]

    val nullPoint   = Point(1000, 1000)
    val nullPoints  = Vector(nullPoint)
    val nullValue   = Int.MaxValue - 1
    slots.sortBy(_.idealPixels).foreach(slot => {
      val idealPixels = slot.idealPixels.toInt
      val idealTiles = (idealPixels + 16) / 32
      val flyer = slot.unitClass.isFlyer && ! slot.unitClass.isFlyingBuilding && slot.unitClass != Protoss.Shuttle
      if (enemyRangePixelsMin < 32 && zone.exitNow.isDefined && idealPixels <= Math.max(32, enemyRangePixelsMin)) {
        // Against enemy melee units, place melee units directly into the exit
        val nextPixel = start
          .project(
            // Point towards the left/right side of the choke
            zone.exitNow.get.sidePixels(meleeSlots.size % 2),
            // If odd-sized row: First unit goes in the middle; otherwise offset by half a melee radius
            (if (meleeChokeWidthUnits % 2 == 0) meleeUnitDiameter / 2 else 0)
            // Project unit towards the current side
            +  meleeUnitDiameter * ((meleeSlots.size / 2) % meleeChokeWidthUnits)) +
          // Fill in rows from front to back
          start.project(end, meleeUnitDiameter * (2 + (meleeSlots.size / meleeChokeWidthUnits))) -
          start

        meleeSlots += ((slot.unitClass, nextPixel))
        if ( ! flyer) {
          val w = slot.unitClass.width / 2
          val h = slot.unitClass.height / 2
          occupied.set(nextPixel.add(+w, +h).tileIncluding, true)
          occupied.set(nextPixel.add(-w, +h).tileIncluding, true)
          occupied.set(nextPixel.add(+w, -h).tileIncluding, true)
          occupied.set(nextPixel.add(-w, -h).tileIncluding, true)
        }
      }
      else {
        // Apply arc positioning
        // Find the best point for this unit
        val distanceGrid = zone.exitNow.map(_.distanceGrid).getOrElse(zone.exitDistanceGrid)
        val point = (Spiral.points(11) ++ nullPoints).minBy(p => {
          if (p == nullPoint) {
            nullValue
          } else {
            val tile = startTile.add(p)
            if (tile.valid
              && (flyer || (
                zone.tileGrid.get(tile)
                && ! occupied.get(tile)
                && With.grids.walkable.get(tile)
                && ! With.architecture.unbuildable.get(tile)
                && ! With.architecture.untownhallable.get(tile)))) {
              val exitDistance                = distanceGrid.get(tile)
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
          if ( ! flyer) {
            occupied.set(tile, true)
          }
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
