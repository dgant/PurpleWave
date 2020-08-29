package Placement

import Lifecycle.With
import Mathematics.Points.{Direction, Point, Tile, TileGenerator}
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Preplacer {

  private lazy val points = Array.fill[Option[PreplacementSlot]](With.mapTileWidth * With.mapTileHeight)(None)
  private val byDimensions = new mutable.HashMap[(Int, Int), ArrayBuffer[Tile]]
  private val byBuilding = new mutable.HashMap[UnitClass, ArrayBuffer[Tile]]

  def get(width: Int, height: Int): Seq[Tile] = byDimensions.getOrElse((width, height), Seq.empty)
  def get(building: UnitClass): Seq[Tile] = byBuilding.getOrElse(building, Seq.empty)

  def fit(from: Tile, direction: Direction, template: PreplacementTemplate, maxFits: Int = 1): Seq[Fit] = {
    val output = new ArrayBuffer[Fit]
    val zone = from.zone
    val generator = new TileGenerator(from, zone.boundary.startInclusive, zone.boundary.endExclusive, direction)
    while(output.size < maxFits && generator.hasNext) {
      val tile = generator.next()
      if (fits(tile, template)) {
        val newFit = Fit(tile, template)
        output += newFit
        place(newFit)
      }
    }
    output
  }

  def fits(origin: Tile, template: PreplacementTemplate): Boolean = {
    // TODO: Allow rechecking an existing fit
    val violation = template.points.find(templatePoint => {
      val slot = templatePoint.requirement
      val slotOrigin = origin.add(templatePoint.point)
      var violated = false
      for (dx <- 0 until slot.width) {
        for (dy <- 0 until slot.height) {
          val relative = Point(dx, dy)
          val tile = slotOrigin.add(relative)
          if ( ! tile.valid) {
            violated = true
          } else if ( ! tile.add(slot.width - 1, slot.height - 1).valid) {
            violated = true
          } else {
            val previous = points(tile.i)
            if (slot.requireWalkable) {
              if ( ! With.grids.walkable.get(tile)) {
                violated = true
              } else  if (previous.exists(!_.requirement.walkableAfter)) {
                violated = true
              }
            }
            if (slot.requireBuildable) {
              if ( ! With.grids.buildable.get(tile)) {
                violated = true
              }
              if (previous.exists( ! _.requirement.buildableAfter)) {
                if (previous.exists(prev => prev.point != relative)) {
                  violated = true
                }
                if (previous.exists(_.requirement.width != slot.width)) {
                  violated = true
                }
                if (previous.exists(_.requirement.height != slot.height)) {
                  violated = true
                }
                if (slot.buildings.nonEmpty && ! previous.forall(_.requirement.buildings.forall(slot.buildings.contains))) {
                  violated = true
                }
              }
            }
          }
        }
      }
      violated
    })

    violation.isEmpty
  }

  def place(fit: Fit): Unit = {
    fit.template.points.foreach(p =>
      (0 until p.requirement.width).foreach(dx =>
        (0 until p.requirement.height).foreach(dy => {
          val tile = fit.origin.add(p.point).add(dx, dy)
          if ( ! tile.valid ) {
            With.logger.warn("Attempted to place invalid tile: " + tile + " for " + fit)
          } else {
            points(tile.i) = Some(PreplacementSlot(Point(dx, dy), p.requirement))
          }
        })
      )
    )
    fit.template.points.foreach(p => place(fit.origin.add(p.point), p.requirement))
  }

  def place(tile: Tile, requirement: PreplacementRequirement): Unit = {
    if ( ! requirement.requireBuildable) return
    if (requirement.buildings.nonEmpty) {
      requirement.buildings.foreach(b => {
        byBuilding.put(b, byBuilding.getOrElse(b, new ArrayBuffer[Tile]))
        byBuilding(b) += tile
      })
    } else {
      val d = (requirement.width, requirement.height)
      byDimensions.put(d, byDimensions.getOrElse(d, new ArrayBuffer[Tile]))
      byDimensions(d) += tile
    }
  }
}
