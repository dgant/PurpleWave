package Macro.Architecture.PlacementRequests

import Mathematics.Points.Tile

abstract class PlacementTask {
  def tiles: Seq[Tile]
  def retain(): Boolean
  def accept(tile: Tile): Boolean
  def score(tile: Tile): Double
  def step(): Option[PlacementResult]
}
