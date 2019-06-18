package Macro.Architecture.PlacementRequests

abstract class PlacementTask {
  def step(): Option[PlacementResult]
}
