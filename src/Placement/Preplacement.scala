package Placement


import Lifecycle.With
import Mathematics.Points.{Direction, Point, Tile, TileGenerator}

import scala.collection.mutable.ArrayBuffer

class Preplacement {

  private lazy val points = new Array[Option[PreplacementPoint]](With.mapTileWidth * With.mapTileHeight)

  def fit(from: Tile, direction: Direction, template: PreplacementTemplate, maxFits: Int = 1): Seq[Fit] = {
    val output = new ArrayBuffer[Fit]
    val zone = from.zone
    val generator = new TileGenerator(from, zone.boundary.startInclusive, zone.boundary.endExclusive, direction)
    while(output.size < maxFits && generator.hasNext) {
      val tile = generator.next()
      if (fits(tile, template)) {
        output += new Fit(tile, template)
      }
    }
    output
  }

  def fits(origin: Tile, template: PreplacementTemplate): Boolean = {
    // TODO: Allow rechecking an existing fit
    val violation = template.points.find(p => {
      val slot = p.slot
      val slotOrigin = origin.add(p.point)
      for (dx <- 0 until slot.width) {
        for (dy <- 0 until slot.height) {
          val relative = Point(dx, dy)
          val tile = slotOrigin.add(relative)
          if ( ! tile.valid) {
            return true
          }
          val previous = points(tile.i)
          if (slot.requireWalkable) {
            if ( ! With.grids.walkable.get(tile)) {
              return true
            }
            if (previous.exists( ! _.slot.walkableAfter)) {
              return true
            }
          }
          if (slot.requireBuildable) {
            if ( ! With.grids.buildable.get(tile)) {
              return true
            }
            if (previous.exists( ! _.slot.buildableAfter)) {
              if (previous.exists(prev => prev.point != relative)) {
                return true
              }
              if (previous.exists(_.slot.width != slot.width)) {
                return true
              }
              if (previous.exists(_.slot.height != slot.height)) {
                return true
              }
              if (slot.buildings.nonEmpty && ! previous.forall(_.slot.buildings.forall(slot.buildings.contains))) {
                return true
              }
            }
          }

        }
      }
      false
    })

    violation.isEmpty
  }
}
