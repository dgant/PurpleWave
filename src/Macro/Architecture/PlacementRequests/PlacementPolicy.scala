package Macro.Architecture.PlacementRequests

import Mathematics.Points.Tile

abstract class PlacementPolicy {
  def tiles: Seq[Tile]
  def retain(): Boolean
  def accept(tile: Tile): Boolean
  def score(tile: Tile): Double
}
