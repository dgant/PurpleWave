package Macro.Architecture.PlacementRequests

import Lifecycle.With
import Macro.Architecture.Tiles.Surveyor
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile
import Utilities.ByOption

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class PlacementTaskClassic(request: PlacementRequest) extends PlacementTask {
  private val blueprint             = request.blueprint
  private var candidatesUnfiltered  : Option[ArrayBuffer[Tile]] = None
  private var candidatesFiltered    : Option[ArrayBuffer[Tile]] = None
  private var nextFilteringIndex    = 0
  private var nextEvaluationIndex   = 0
  private val evaluationValues      = new mutable.HashMap[Tile, Double]
  private val evaluationStartFrame  = With.frame

  override def tiles: Seq[Tile] = {
    Surveyor.candidates(blueprint).view.flatMap(_.tiles(blueprint))
  }

  override def retain(): Boolean = {
    (request.tile.exists(request.blueprint.accepts(_, Some(request)))
      && request.blueprint.requireTownHallTile.contains(false) // Town halls are so important (and inexpensive) that we should always recalculate
      && request.result.forall(result => With.framesSince(result.frameFinished) < With.configuration.buildingPlacementRefreshPeriod))
  }

  override def accept(tile: Tile): Boolean = {
    blueprint.accepts(tile, Some(request))
  }

  override def score(tile: Tile): Double = {
    HeuristicMathMultiplicative.resolve(
      blueprint,
      blueprint.placement.get.weightedHeuristics,
      tile)
  }

  def step(): Option[PlacementResult] = {
    if (stillSurveying) {
      val sources = tiles
      candidatesUnfiltered = Some(new ArrayBuffer[Tile] ++ tiles)
      candidatesFiltered   = Some(new ArrayBuffer[Tile])
    }
    else if (stillFiltering) {
      // Filter them (in batches)
      var filterCount = 0
      val filterCountMax = batchSize
      while (stillFiltering && filterCount < filterCountMax) {

        val candidate = candidatesUnfiltered.get(nextFilteringIndex)
        if (accept(candidate)) {
          candidatesFiltered.get += candidate
        }

        filterCount         += 1
        nextFilteringIndex  += 1
      }
    }
    else if (stillEvaluating) {
      // Evaluate them (in batches)
      var evaluationCount = 0
      val evaluationCountMax = batchSize
      while (stillEvaluating && evaluationCount < evaluationCountMax) {
        val candidate = candidatesFiltered.get(nextEvaluationIndex)
        evaluationValues(candidate) = score(candidate)
        evaluationCount     += 1
        nextEvaluationIndex += 1
      }
    }
    else {
      // We've evaluated all the tiles! Return our placement conclusions.
      val best = ByOption.minBy(evaluationValues)(_._2).map(_._1)
      val output = PlacementResult(
        request,
        best,
        frameStarted      = evaluationStartFrame,
        frameFinished     = With.frame,
        candidates        = candidatesUnfiltered.get.size,
        evaluated         = candidatesFiltered.get.size)
      return Some(output)
    }
    None
  }

  private def batchSize: Int =
    if (With.frame < With.configuration.buildingPlacementBatchingStartFrame)
      Int.MaxValue
    else
      With.configuration.buildingPlacementBatchSize

  private def stillSurveying  : Boolean = candidatesUnfiltered.isEmpty
  private def stillFiltering  : Boolean = candidatesUnfiltered.exists(nextFilteringIndex < _.length) && candidatesFiltered.get.length < With.configuration.buildingPlacementMaxTilesToEvaluate
  private def stillEvaluating : Boolean = candidatesFiltered.exists(nextEvaluationIndex < _.length)
}
