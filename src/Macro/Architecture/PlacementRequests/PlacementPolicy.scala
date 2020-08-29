package Macro.Architecture.PlacementRequests

import Macro.Architecture.Tiles.Surveyor
import Mathematics.Points.Tile

abstract class PlacementPolicy {
  /**
    * @return Tiers of tiles to consider for placement.
    *         If a tier contains any acceptable tiles, use one of those and ignore succeeding tiers.
    */
  def tiles: Seq[Seq[Tile]]
  def retain(): Boolean
  def accept(tile: Tile): Boolean
  def score(tile: Tile): Double

  protected def tilesForRequest(request: PlacementRequest): Seq[Seq[Tile]] = {
    Surveyor.candidates(request.blueprint).view.map(_.view.flatMap(_.tiles(request.blueprint)))
  }
}
