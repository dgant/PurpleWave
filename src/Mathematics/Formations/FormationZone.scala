package Mathematics.Formations

import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Points._
import Mathematics.Shapes.Spiral
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.{ByOption, Minutes}

import scala.collection.mutable

class FormationZone(zone: Zone, edge: Edge) {

  def form(units: Seq[FriendlyUnitInfo]): FormationAssigned = {
    if (units.isEmpty) return new FormationAssigned(Map.empty)

    val occupied = With.grids.disposableBoolean1()
    val slots    = units.map(new FormationSlot(_))
    val chokeEnd = edge.pixelCenter.project(zone.exitNow.map(_.pixelTowards(zone)).getOrElse(zone.centroid.pixelCenter), 300)

    // Avoid colliding with stuff
    zone.bases.flatMap(_.townHallArea.tiles).foreach(occupied.set(_, true))
    zone.bases.foreach(base => if (base.workerCount > 2) base.harvestingArea.tiles.foreach(occupied.set(_, true)))

    // Clear a line for Scarabs
    if (units.exists(Protoss.Reaver)) {
      PixelRay(edge.pixelCenter, chokeEnd).foreach(occupied.set(_, true))
    }
    val expectedRange: Int = 32 * (
      if (With.frame > Minutes(4)() && With.enemies.exists(_.isProtoss)) 6
      else if (With.enemies.exists(_.isTerran)) 4
      else 1)
    val enemyRangePixelsMax   : Int = Math.max(expectedRange, ByOption.max(With.units.enemy.filter(_.unitClass.attacksGround).view.map(_.effectiveRangePixels.toInt)).getOrElse(0))
    val meleeUnitSize         : Int = 5 + Math.max(16, slots.map(s => if (s.rangePixels > 32) 0 else if (edge.direction.isHorizontal) s.unitClass.width else s.unitClass.height).max)
    val meleeChokeWidthUnits  : Int = Math.max(1, Math.min(2 * edge.radiusPixels.toInt / meleeUnitSize, slots.count(_.rangePixels <= 32)))
    val altitudeInside    = zone.centroid.altitude
    val altitudeOutside   = edge.otherSideof(zone).centroid.altitude
    val altitudeRequired  = if (enemyRangePixelsMax > 32 && altitudeInside > altitudeOutside) Some(altitudeInside) else None

    val meleeSlotsEmpty = units.indices.map(i => {
      val unitsInThisRow = i % meleeChokeWidthUnits
      val rowsFilled = i / meleeChokeWidthUnits
      val targetSide = edge.sidePixels(unitsInThisRow % 2)
      val vectorLateral = if (meleeChokeWidthUnits % 2 == 0) {
        edge.pixelCenter.project(targetSide, meleeUnitSize * (0.5 + unitsInThisRow / 2)) - edge.pixelCenter
      } else {
        edge.pixelCenter.project(targetSide, meleeUnitSize * ((1 + unitsInThisRow) / 2)) - edge.pixelCenter
      }
      val vectorDepth = edge.pixelCenter.project(chokeEnd, 24 + meleeUnitSize * rowsFilled) - edge.pixelCenter
      edge.pixelCenter + vectorLateral + vectorDepth
    })

    val meleeSlots = new mutable.ArrayBuffer[(UnitClass, Pixel)]
    val arcSlots   = new mutable.ArrayBuffer[(UnitClass, Pixel)]

    val escapeAir = With.geography.home.pixelCenter
    val escapeGround = ByOption.minBy(zone.edges.view.filterNot(_ == edge).map(_.pixelCenter))(_.groundPixels(With.geography.home)).getOrElse(zone.centroid.pixelCenter)
    slots.sortBy(_.rangePixels).foreach(slot => {
      val escape = if (slot.unitClass.isFlyer) escapeAir else escapeGround
      val rangeTiles = slot.rangePixels.toInt / 32
      val flyer = slot.unitClass.isFlyer && ! slot.unitClass.isFlyingBuilding && slot.unitClass != Protoss.Shuttle
      if (altitudeRequired.isEmpty && enemyRangePixelsMax <= 32 && slot.rangePixels <= 32) {
        // Against enemy melee units, place melee units directly into the exit
        val nextPixel = meleeSlotsEmpty(meleeSlots.size)
        meleeSlots += ((slot.unitClass, nextPixel))
        if ( ! flyer) {
          val w = slot.unitClass.width / 2
          val h = slot.unitClass.height / 2
          occupied.set(nextPixel.add(+w, +h).tile, true)
          occupied.set(nextPixel.add(-w, +h).tile, true)
          occupied.set(nextPixel.add(+w, -h).tile, true)
          occupied.set(nextPixel.add(-w, -h).tile, true)
        }
      } else {
        // Apply arc positioning to find the best spot for this unit
        val candidates = Spiral.points(11).view.map(edge.pixelCenter.tile.add).filter(tile =>
          tile.valid
            && tile.zone == zone
            // Stand uphill if possible
            && altitudeRequired.forall(tile.altitude >=)
            // Don't stand in a choke
            && ! zone.edges.exists(e => e.radiusPixels < 96 && e.pixelCenter.pixelDistance(tile.pixelCenter) < e.radiusPixels + 32)
            // Stand in an unoccupied tile
            && (flyer || (tile.walkableUnchecked && ! occupied.get(tile) && ! With.groundskeeper.isReserved(tile))))
        val bestTile = ByOption.minBy(candidates)(tile => {
          val exitDistanceTiles = edge.pixelCenter.tile.tileDistanceManhattan(tile)
          val exitAttackingCost = Math.max(0, exitDistanceTiles - rangeTiles)
          val exitDefendingCost = Math.max(0, enemyRangePixelsMax / 32 - exitDistanceTiles)
          val leanHomeCost = tile.pixelCenter.pixelDistance(escape) / 1000
          Math.max(exitAttackingCost, exitDefendingCost) + leanHomeCost
        })
        bestTile.foreach(tile => {
          if ( ! flyer) { occupied.set(tile, true) }
          arcSlots += ((slot.unitClass, tile.pixelCenter))
        })
      }
    })

    new FormationUnassigned(
      (meleeSlots.view ++ arcSlots.view)
        .groupBy(_._1)
        .mapValues(_.map(_._2)))
      .assign(units)
  }
}
