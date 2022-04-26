package Placement.Generation

import Lifecycle.With
import Mathematics.Points._
import Placement.Access.Fits
import Placement.Templating.{Template, TemplatePoint, TemplatePointRequirement}

import scala.collection.mutable.ArrayBuffer

trait Fitter extends Fits {

  /**
    * Attempts to fit a single template.
    */
  def fitAndIndex(order: Int, from: Tile, bounds: TileRectangle, direction: Direction, template: Template, maxFits: Int = 1): Seq[Fit] = {
    fitAndIndexAll(order, from, bounds, direction, Seq(template), maxFits)
  }

  /**
    * Attempts to fit each of a sequence of templates.
    */
  def fitAndIndexAll(order: Int, from: Tile, bounds: TileRectangle, direction: Direction, templates: Seq[Template], maxFits: Int = 1): Seq[Fit] = {
    val output = new ArrayBuffer[Fit]

    var templateIndex = 0
    while (templateIndex < templates.length) {
      val template = templates(templateIndex)
      val generator = new TileGenerator(from, bounds.startInclusive, bounds.endExclusive, direction)
      while (generator.hasNext) {
        val tile = generator.next()
        if (fitsAt(template, tile)) {
          val newFit = Fit(tile, template, order)
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
    if ( ! template.accept(origin)) {
      return false
    }
    val violation = template.points.find( ! accept(_, origin))
    violation.isEmpty
  }

  private def accept(templatePoint: TemplatePoint, origin: Tile): Boolean = {
    val requirement = templatePoint.requirement
    val pointTile = origin.add(templatePoint.point)
    if (requirement.isTownHall) {
      return pointTile.base.exists(_.townHallTile == pointTile)
    } else if (requirement.isGas) {
      return pointTile.base.exists(_.gas.exists(_.tileTopLeft == pointTile))
    } else {
      for (dx <- 0 until requirement.width) {
        for (dy <- 0 until requirement.height) {
          val relative = Point(dx, dy)
          val tile = pointTile.add(relative)
          if ( ! accept(requirement, relative, tile)) {
            return false
          }
        }
      }
    }
    true
  }

  private def accept(requirement: TemplatePointRequirement, relative: Point, tile: Tile): Boolean = {
    if ( ! tile.valid) {
      return false
    } else if ( ! tile.add(requirement.width - 1, requirement.height - 1).valid) {
      return false
    } else {
      val previous = at(tile)
      if (requirement.walkableBefore) {
        if ( ! tile.walkableUnchecked) {
          return false
        } else if ( ! previous.requirement.walkableAfter) {
          return false
        }
      }
      if (requirement.buildableBefore) {
        if ( ! With.grids.buildable.get(tile)) {
          return false
        }
        if ( ! previous.requirement.buildableAfter) {
          if (previous.point != relative) {
            return false
          }
          if (previous.requirement.width != requirement.width) {
            return false
          }
          if (previous.requirement.height != requirement.height) {
            return false
          }
          if (requirement.buildings.nonEmpty && ! previous.requirement.buildings.forall(requirement.buildings.contains)) {
            return false
          }
        }
      }
    }
    true
  }
}
