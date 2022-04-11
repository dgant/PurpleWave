package Placement.Generation

import Lifecycle.With
import Mathematics.Points._
import Placement.Access.Fits
import Placement.Templating.Template

import scala.collection.mutable.ArrayBuffer

trait Fitter extends Fits {

  /**
    * Attempts to fit a single template.
    */
  def fitAndIndex(from: Tile, bounds: TileRectangle, direction: Direction, template: Template, maxFits: Int = 1): Seq[Fit] = {
    fitAndIndexAll(from, bounds, direction, Seq(template), maxFits)
  }

  /**
    * Attempts to fit each of a sequence of templates.
    */
  def fitAndIndexAll(from: Tile, bounds: TileRectangle, direction: Direction, templates: Seq[Template], maxFits: Int = 1): Seq[Fit] = {
    val output = new ArrayBuffer[Fit]

    var templateIndex = 0
    while (templateIndex < templates.length) {
      val template = templates(templateIndex)
      val generator = new TileGenerator(from, bounds.startInclusive, bounds.endExclusive, direction)
      while (generator.hasNext) {
        val tile = generator.next()
        if (fitsAt(template, tile)) {
          val newFit = Fit(tile, template)
          index(newFit)
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

  /**
    * Does this template fit at this tile?
    * Considers conflicts with existing fits.
    */
  def fitsAt(template: Template, origin: Tile): Boolean = {
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
            val previous = at(tile)
            if (slot.walkableBefore) {
              if ( ! With.grids.walkable.get(tile)) {
                violated = true
              } else if ( ! previous.requirement.walkableAfter) {
                violated = true
              }
            }
            if (slot.buildableBefore) {
              if ( ! With.grids.buildable.get(tile)) {
                violated = true
              }
              if ( ! previous.requirement.buildableAfter) {
                if (previous.point != relative) {
                  violated = true
                }
                if (previous.requirement.width != slot.width) {
                  violated = true
                }
                if (previous.requirement.height != slot.height) {
                  violated = true
                }
                if (slot.buildings.nonEmpty && ! previous.requirement.buildings.forall(slot.buildings.contains)) {
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
}
