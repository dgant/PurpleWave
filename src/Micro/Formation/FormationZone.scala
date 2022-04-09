package Micro.Formation

import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points._
import Mathematics.Shapes.Spiral
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass
import Tactic.Squads.FriendlyUnitGroup
import Utilities.Time.Minutes

import scala.collection.mutable

object FormationZone {
  def apply(group: FriendlyUnitGroup, zone: Zone, edge: Edge): Formation = {
    new FormationStandard(group, FormationStyleGuard, edge.pixelCenter, Some(zone))
  }

  def oldApply(group: FriendlyUnitGroup, zone: Zone, edge: Edge): Formation = {
    // Notes: These are the restrictions used
    // [x] Don't stand in harvesting area (TBH this should just be mining path, as harvesting area is massive when gas is opposite minerals)
    // [-] Don't stand in town hall area, which would block construction
    // - If we have Reavers, leave a path up the middle for Scarabs
    // [x] Min distance = expected enemy range
    // [x] If expected enemy range > 32px and we are ramped, require upstairs tiles
    // [x] Don't stand in groundskeeper-reserved tiles
    //
    // We probably also want to:
    // - Preserve melee plug against melee enemies
    // - Arc: Replace min/max distance with ordered depth tiers

    val units = group.groupFriendlyOrderable
    if (units.isEmpty) return FormationEmpty

    val occupied = With.grids.disposableBoolean()
    val chokeEnd = edge.pixelCenter.project(zone.exitNow.map(_.pixelTowards(zone)).getOrElse(zone.centroid.center), 300)

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
    val enemyRangePixelsMax   : Int = Math.max(expectedRange, Maff.max(With.units.enemy.filter(_.unitClass.attacksGround).view.map(_.effectiveRangePixels.toInt)).getOrElse(0))
    val meleeUnitSize         : Int = 5 + Math.max(16, units.view.map(u => if (u.formationRangePixels > 32) 0 else if (edge.direction.isHorizontal) u.unitClass.width else u.unitClass.height).max)
    val meleeChokeWidthUnits  : Int = Math.max(1, Math.min(2 * edge.radiusPixels.toInt / meleeUnitSize, units.count(_.formationRangePixels <= 32)))
    val altitudeInside    = zone.centroid.altitude
    val altitudeOutside   = edge.otherSideof(zone).centroid.altitude
    val altitudeRequired  = if (enemyRangePixelsMax > 32 && altitudeInside > altitudeOutside) Some(altitudeInside) else None
    val nearSidePixel     = edge.sidePixels.minBy(_.pixelDistanceSquared(With.geography.home.center))
    val maxSightPixels    = units.view.map(_.sightPixels).max
    val formationCenter   = if (maxSightPixels >= edge.radiusPixels) edge.pixelCenter else nearSidePixel.project(edge.pixelCenter, maxSightPixels)

    val meleeSlotsEmpty = units.view.zipWithIndex.map(ui => {
      val u = ui._1
      val i = ui._2
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
    }).toIndexedSeq

    val meleeSlots    = new mutable.ArrayBuffer[(UnitClass, Pixel)]
    val arcSlots      = new mutable.ArrayBuffer[(UnitClass, Pixel)]
    val escapeAir     = With.geography.home.center
    val escapeGround  = Maff.minBy(zone.edges.view.filterNot(edge==).map(_.pixelCenter))(_.groundPixels(With.geography.home)).getOrElse(zone.centroid.center)
    units.toVector.sortBy(_.formationRangePixels).foreach(unit => {
      val escape = if (unit.flying) escapeAir else escapeGround
      val rangeTiles = unit.formationRangePixels.toInt / 32
      val flyer = unit.unitClass.isFlyer && ! unit.unitClass.isFlyingBuilding && unit.unitClass != Protoss.Shuttle
      if (altitudeRequired.isEmpty && enemyRangePixelsMax <= 32 && unit.formationRangePixels <= 32 && edge.radiusPixels <= maxSightPixels) {
        // Against enemy melee units, place melee units directly into the exit
        val nextPixel = meleeSlotsEmpty(meleeSlots.size)
        meleeSlots += ((unit.unitClass, nextPixel))
        if ( ! flyer) {
          val w = unit.unitClass.width / 2
          val h = unit.unitClass.height / 2
          occupied.set(nextPixel.add(+w, +h).tile, true)
          occupied.set(nextPixel.add(-w, +h).tile, true)
          occupied.set(nextPixel.add(+w, -h).tile, true)
          occupied.set(nextPixel.add(-w, -h).tile, true)
        }
      } else {
        // Apply arc positioning to find the best spot for this unit
        val candidates = Spiral.points(11).view.map(formationCenter.tile.add).filter(tile =>
          tile.valid
            && tile.zone == zone
            // Stand uphill if possible
            && altitudeRequired.forall(tile.altitude >=)
            // Don't stand in a choke
            && ! zone.edges.exists(e => e.radiusPixels < 96 && e.pixelCenter.pixelDistance(tile.center) < e.radiusPixels + 32)
            // Stand in an unoccupied tile
            && (flyer || (tile.walkableUnchecked && ! occupied.get(tile) && With.groundskeeper.isFree(tile)))
        )
        val bestTile = Maff.minBy(candidates)(tile => {
          val exitDistanceTiles = tile.center.pixelDistance(Maff.projectedPointOnSegment(tile.center, edge.sidePixels.head, edge.sidePixels.last)).toInt / 32
          val exitAttackingCost = Math.max(0, exitDistanceTiles - rangeTiles)
          val exitDefendingCost = Math.max(0, enemyRangePixelsMax / 32 - exitDistanceTiles)
          val leanHomeCost = tile.center.pixelDistance(escape) / 1000
          Math.max(exitAttackingCost, exitDefendingCost) + leanHomeCost
        })
        bestTile.foreach(tile => {
          if ( ! flyer) { occupied.set(tile, true) }
          arcSlots += ((unit.unitClass, tile.center))
        })
      }
    })

    val unassigned = UnassignedFormation(
      FormationStyleGuard,
      (meleeSlots.view ++ arcSlots.view)
        .groupBy(_._1)
        .mapValues(_.map(_._2)),
      group)
    val output = unassigned.outwardFromCentroid
    FormationBasic(FormationStyleGuard, output)
  }
}
