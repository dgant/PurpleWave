package Placement

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points._
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Preplacer {

  private lazy val points = Array.fill[Option[PreplacementSlot]](With.mapTileWidth * With.mapTileHeight)(None)
  private val byDimensions = new mutable.HashMap[(Int, Int), ArrayBuffer[Tile]]
  private val byBuilding = new mutable.HashMap[UnitClass, ArrayBuffer[Tile]]
  private val byZone = new mutable.HashMap[Zone, ArrayBuffer[PreplacementRequirement]]()

  def get(width: Int, height: Int): Seq[Tile] = byDimensions.getOrElse((width, height), Seq.empty)
  def get(building: UnitClass): Seq[Tile] = byBuilding.getOrElse(building, Seq.empty)

  def fitAny(from: Tile, bounds: TileRectangle, direction: Direction, templates: Seq[PreplacementTemplate], maxFits: Int = 1): Seq[Fit] = {
    val output = new ArrayBuffer[Fit]

    var templateIndex = 0
    while (templateIndex < templates.length) {
      val template = templates(templateIndex)
      val generator = new TileGenerator(from, bounds.startInclusive, bounds.endExclusive, direction)
      while (generator.hasNext) {
        val tile = generator.next()
        if (fits(tile, template)) {
          val newFit = Fit(tile, template)
          place(newFit)
          output += newFit
          if (output.length >= maxFits) {
            return output
          }
        }
      }
      templateIndex += 1
    }
    output
  }

  def fit(from: Tile, bounds: TileRectangle, direction: Direction, template: PreplacementTemplate, maxFits: Int = 1): Seq[Fit] = {
    fitAny(from, bounds, direction, Seq(template), maxFits)
  }

  def fits(origin: Tile, template: PreplacementTemplate): Boolean = {
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
    byZone.put(tile.zone, byZone.getOrElse(tile.zone, new ArrayBuffer[PreplacementRequirement]()))
    byZone(tile.zone) += requirement
  }
}
