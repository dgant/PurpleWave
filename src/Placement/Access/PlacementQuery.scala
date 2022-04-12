package Placement.Access

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile

import scala.collection.mutable

class PlacementQuery {
  var requirements  = new PlacementQueryOptions
  var preferences   = new PlacementQueryOptions

  def accept(tile: Tile): Boolean = {
    if ( ! tile.valid) return false
    accept(Foundation(tile, With.placement.at(tile)))
  }

  def accept(foundation: Foundation): Boolean = {
    requirements.accept(foundation)
  }

  def score(foundation: Foundation): Double = {
    preferences.score(foundation)
  }

  def query: Traversable[Foundation] = {
    // Start with the smallest matching collection
    val foundationsRequired = Maff.minBy(requirements.label.map(With.placement.get)
      ++ requirements.zone.map(With.placement.get)
      ++ requirements.base.map(With.placement.get)
      ++ requirements.width.flatMap(w => requirements.height.map(h => With.placement.get(w, h)))
      ++ requirements.building.map(With.placement.get))(_.length)
    lazy val foundationsByTile = Seq(requirements.tile
      .view
      .filter(_.valid)
      .map(tile => Foundation(tile, With.placement.at(tile)))
      .filter(p => p.point.requirement.buildableBefore
              && ! p.point.requirement.buildableAfter))
    val filteredOnce = Maff.orElse(foundationsRequired, foundationsByTile, Seq(With.placement.foundations))
    val filteredOnceSmallest = filteredOnce.minBy(_.length)
    val filteredCompletely = filteredOnceSmallest.view.filter(accept)
    val output = new mutable.PriorityQueue[Foundation]()(Ordering.by(preferences.score))
    output ++= filteredCompletely
    output
  }

  def apply: Traversable[Tile] = query.view.map(_.tile)
}
