package Macro.Architecture

import Lifecycle.With
import Macro.Architecture.Heuristics.PlacementHeuristicEvaluation
import Mathematics.Points.Tile

import scala.collection.mutable

case class Placement(
  blueprint         : Blueprint,
  tile              : Option[Tile],
  evaluations       : Iterable[PlacementHeuristicEvaluation],
  scoresByTile      : mutable.HashMap[Tile, Double],
  totalNanoseconds  : Long,
  frameStarted      : Int,
  frameFinished     : Int,
  candidates        : Int,
  evaluated         : Int) {

  def satisfies(blueprint: Blueprint): Boolean = {
    With.framesSince(frameFinished) < With.configuration.maxPlacementAgeFrames && tile.exists(blueprint.accepts)
  }
}
