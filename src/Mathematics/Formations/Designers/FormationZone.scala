package Mathematics.Formations.Designers

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Formations.{FormationAssigned, FormationSlot, FormationUnassigned}
import Mathematics.Points.{Pixel, PixelRay, Point}
import Mathematics.Shapes.Spiral
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

import scala.collection.mutable

class FormationZone(zone: Zone, enemies: Seq[UnitInfo]) extends FormationDesigner {

  override def form(units: Seq[FriendlyUnitInfo]): FormationAssigned = {
    if (units.isEmpty) return new FormationAssigned(Map.empty)

    val occupied        = With.grids.disposableBoolean1()
    val slots           = units.map(new FormationSlot(_))
    val chokeSides      = zone.exitNow.get.sidePixels
    val chokeCenter     = zone.exitNow.map(_.pixelCenter).getOrElse(zone.centroid.pixelCenter)
    val chokeEnd        = chokeCenter.project(zone.exitNow.map(_.pixelTowards(zone)).getOrElse(zone.centroid.pixelCenter), 300)
    val chokeCenterTile = chokeCenter.tileIncluding

    // Avoid colliding with stuff
    zone.bases.flatMap(_.townHallArea.tiles).foreach(occupied.set(_, true))
    zone.bases.foreach(base => if (base.workerCount > 2) base.harvestingArea.tiles.foreach(occupied.set(_, true)))
    // Clear a line for Scarabs
    if (units.exists(_.is(Protoss.Reaver))) {
      PixelRay(chokeCenter, chokeEnd).tilesIntersected.foreach(occupied.set(_, true))
    }

    val allEnemies = With.units.enemy.view.filter(_.attacksAgainstGround > 0)
    val enemyRangePixelsMin   : Int = ByOption.min(allEnemies.view.map(_.effectiveRangePixels.toInt)).getOrElse(if (With.enemy.isTerran) 5 * 32 else 32)
    val enemyRangePixelsMax   : Int = ByOption.max(allEnemies.view.map(_.effectiveRangePixels.toInt)).getOrElse(32 * 5)
    val meleeUnitDiameter     : Int = 6 + Math.max(16, slots.map(s => if (s.idealPixels > 32) 0 else s.unitClass.dimensionMax.toInt).max)
    val meleeChokeWidthUnits  : Int = Math.max(1, Math.min(2 * zone.exitNow.map(_.radiusPixels.toInt).getOrElse(0) / meleeUnitDiameter, slots.count(_.idealPixels <= 32)))

    // TODO: Standardize definition of a melee slot

    val meleeSlotsEmpty = units.indices.map(i => {
      val unitsInThisRow = i % meleeChokeWidthUnits
      val rowsFilled = i / meleeChokeWidthUnits
      val targetSide = chokeSides(unitsInThisRow % 2)
      val vectorLateral = if (meleeChokeWidthUnits % 2 == 0) {
        chokeCenter.project(targetSide, meleeUnitDiameter * (0.5 + unitsInThisRow / 2)) - chokeCenter
      } else {
        chokeCenter.project(targetSide, meleeUnitDiameter * ((1 + unitsInThisRow) / 2)) - chokeCenter
      }
      val vectorDepth = chokeCenter.project(chokeEnd, 12 + meleeUnitDiameter * rowsFilled) - chokeCenter
      val output = chokeCenter + vectorLateral + vectorDepth
      output
    })

    val meleeSlots = new mutable.ArrayBuffer[(UnitClass, Pixel)]
    val arcSlots   = new mutable.ArrayBuffer[(UnitClass, Pixel)]

    val nullPoint   = Point(1000, 1000)
    val nullPoints  = Vector(nullPoint)
    val nullValue   = Int.MaxValue - 1

    slots.sortBy(_.idealPixels).foreach(slot => {
      val idealPixels = slot.idealPixels.toInt
      val idealTiles = (idealPixels + 16) / 32
      val flyer = slot.unitClass.isFlyer && ! slot.unitClass.isFlyingBuilding && slot.unitClass != Protoss.Shuttle
      if (enemyRangePixelsMin <= 32 && zone.exitNow.isDefined && idealPixels <= Math.max(32, enemyRangePixelsMin)) {
        // Against enemy melee units, place melee units directly into the exit
        val nextPixel = meleeSlotsEmpty(meleeSlots.size)
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
            val tile = chokeCenterTile.add(p)
            if (tile.valid
              && (flyer || (
                zone.tileGrid.get(tile)
                && ! occupied.get(tile)
                && With.grids.walkable.get(tile)
                && ! With.architecture.unbuildable.get(tile)
                && ! With.architecture.untownhallable.get(tile)))) {
              val exitDistance                = distanceGrid.get(tile)
              val distanceIntoEnemyRangeNow   = 1 + With.grids.enemyRangeGround.get(tile) - With.grids.enemyRangeGround.addedRange
              val distanceIntoEnemyRangeExit  = Math.max(0, enemyRangePixelsMax / 32 - exitDistance)
              val distanceOutOfOurRange       = Math.max(0, exitDistance - idealTiles)
              5 * distanceIntoEnemyRangeNow + 4 * distanceIntoEnemyRangeExit + 3 * distanceOutOfOurRange
            } else {
              Int.MaxValue
            }
          }})
        if (point != nullPoint) {
          // Assign the unit
          val tile = chokeCenterTile.add(point)
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
