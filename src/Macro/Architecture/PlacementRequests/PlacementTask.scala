package Macro.Architecture.PlacementRequests

abstract class PlacementTask {
  //def score(tile: Tile): Double
  def step(): Option[PlacementResult]
}
