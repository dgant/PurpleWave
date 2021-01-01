package Mathematics.Formations.Designers

import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Formations.{FormationAssigned, FormationSlot, FormationUnassigned}
import Mathematics.Points._
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
      PixelRay(chokeCenter, chokeEnd).foreach(occupied.set(_, true))
    }

    val allEnemies = With.units.enemy.view.filter(_.attacksAgainstGround > 0)
    val enemyRangePixelsMin   : Int = ByOption.min(allEnemies.view.map(_.effectiveRangePixels.toInt)).getOrElse(if (With.enemy.isTerran) 5 * 32 else 32)
    val enemyRangePixelsMax   : Int = ByOption.max(allEnemies.view.map(_.effectiveRangePixels.toInt)).getOrElse(32 * 5)
    val exit                  : Option[Edge] = zone.exitNow
    val exitDirection         : Direction = exit.map(e => e.sidePixels.head.subtract(e.sidePixels.last).direction).getOrElse(Directions.Right)
    val meleeUnitSize         : Int = 3 + Math.max(16, slots.map(s => if (s.idealPixels > 32) 0 else if (exitDirection.isHorizontal) s.unitClass.width else s.unitClass.height).max)
    val meleeChokeWidthUnits  : Int = Math.max(1, Math.min(2 * exit.map(_.radiusPixels.toInt).getOrElse(0) / meleeUnitSize, slots.count(_.idealPixels <= 32)))
    val altitudeInside  = zone.centroid.altitude
    val altitudeOutside = zone.exitNow.map(_.otherSideof(zone).centroid).map(_.altitude).getOrElse(altitudeInside)
    val altitudeMinimum = if (enemyRangePixelsMax > 32 && altitudeInside > altitudeOutside) Some(altitudeInside) else None

    // TODO: Standardize definition of a melee slot

    val meleeSlotsEmpty = units.indices.map(i => {
      val unitsInThisRow = i % meleeChokeWidthUnits
      val rowsFilled = i / meleeChokeWidthUnits
      val targetSide = chokeSides(unitsInThisRow % 2)
      val vectorLateral = if (meleeChokeWidthUnits % 2 == 0) {
        chokeCenter.project(targetSide, meleeUnitSize * (0.5 + unitsInThisRow / 2)) - chokeCenter
      } else {
        chokeCenter.project(targetSide, meleeUnitSize * ((1 + unitsInThisRow) / 2)) - chokeCenter
      }
      val vectorDepth = chokeCenter.project(chokeEnd, 12 + meleeUnitSize * rowsFilled) - chokeCenter
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
      if (altitudeMinimum.isEmpty && enemyRangePixelsMin <= 32 && zone.exitNow.isDefined && idealPixels <= Math.max(32, enemyRangePixelsMin)) {
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
        // Apply arc positioning to find the best spot for this unit
        val distanceGrid = zone.exitNow.map(_.distanceGrid).getOrElse(zone.exitDistanceGrid)
        val point = (Spiral.points(11) ++ nullPoints).minBy(p => {
          if (p == nullPoint) {
            nullValue
          } else {
            val tile = chokeCenterTile.add(p)
            if (tile.valid
              // Stand uphill if possible
              && altitudeMinimum.forall(tile.altitude >=)
              // Don't stand in a choke
              && ! zone.edges.exists(e => e.radiusPixels < 96 && e.pixelCenter.pixelDistance(tile.pixelCenter) < e.radiusPixels + 32)
              // Stand in an unoccupied tile
              && (flyer || (
                zone.tileGrid.get(tile)
                && tile.walkableUnchecked
                && ! occupied.get(tile)
                && ! With.groundskeeper.isReserved(tile)))) {
              val exitDistance                = distanceGrid.get(tile)
              val distanceIntoEnemyRangeExit  = Math.max(0, enemyRangePixelsMax / 32 - exitDistance)
              val distanceOutOfOurRange       = Math.max(0, exitDistance - idealTiles)
              4 * distanceIntoEnemyRangeExit + 3 * distanceOutOfOurRange
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
