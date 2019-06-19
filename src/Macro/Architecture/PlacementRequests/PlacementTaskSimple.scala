package Macro.Architecture.PlacementRequests
import Macro.Architecture.Tiles.Surveyor
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

class PlacementTaskSimple(request: PlacementRequest) extends PlacementTask {

  override def retain(): Boolean = {
    request.child.forall(_.task().retain())
  }

  override def tiles: Seq[Tile] = {
    Surveyor.candidates(request.blueprint).view.flatMap(_.tiles(request.blueprint))
  }

  override def accept(tile: Tile): Boolean = {
    request.blueprint.accepts(tile, Some(request))
  }

  override def score(tile: Tile): Double = {
    HeuristicMathMultiplicative.resolve(
      request.blueprint,
      request.blueprint.placement.get.weightedHeuristics,
      tile)
  }

  override def step(): Option[PlacementResult] = {
    None
  }
}
