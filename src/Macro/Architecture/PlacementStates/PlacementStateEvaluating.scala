package Macro.Architecture.PlacementStates

import Debugging.Visualizations.Views.Geography.ShowArchitectureHeuristics
import Lifecycle.With
import Macro.Architecture.Heuristics.{EvaluatePlacements, PlacementHeuristicEvaluation}
import Macro.Architecture.Tiles.Surveyor
import Macro.Architecture.{Architect, Blueprint, Placement}
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class PlacementStateEvaluating(blueprint: Blueprint) extends PlacementState {
  
  private var candidatesUnfiltered  : Option[ArrayBuffer[Tile]] = None
  private var candidatesFiltered    : Option[ArrayBuffer[Tile]] = None
  private var nextFilteringIndex    : Int                       = 0
  private var nextEvaluationIndex   : Int                       = 0
  private val evaluations       = new mutable.HashMap[Tile, Iterable[PlacementHeuristicEvaluation]]
  private val evaluationValues  = new mutable.HashMap[Tile, Double]
  
  override def step() {
    if (candidatesUnfiltered.isEmpty) {
      // Figure out which tiles to evaluate
      val sources = Surveyor.candidates(blueprint)
      candidatesUnfiltered  = Some(new ArrayBuffer[Tile])
      candidatesFiltered    = Some(new ArrayBuffer[Tile])
      sources.foreach(source => {
        if (candidatesUnfiltered.size < With.configuration.buildingPlacementMaxTilesToEvaluate) {
          candidatesUnfiltered.get ++= source.tiles(blueprint)
        }
      })
      val candidates = candidatesUnfiltered.get.take(With.configuration.buildingPlacementMaxTilesToEvaluate)
      candidatesUnfiltered = Some(new ArrayBuffer[Tile])
      candidatesUnfiltered.get ++= candidates
    }
    else if (stillFiltering) {
      // Filter them (in batches)
      var evaluationCount = 0
      while (stillFiltering && (
        evaluationCount < With.configuration.buildingPlacementBatchSize
          || With.frame < With.configuration.buildingPlacementBatchingStartFrame)) {
        evaluationCount += 1
        val nextCandidate = candidatesUnfiltered.get(nextFilteringIndex)
        if (Architect.canBuild(blueprint, nextCandidate, recheckPathing = true)) {
          candidatesFiltered.get += nextCandidate
        }
        nextFilteringIndex += 1
      }
    }
    else if (stillEvaluating) {
      // Evaluate them (in batches)
      var evaluationCount = 0
      while (stillEvaluating && (
        evaluationCount < With.configuration.buildingPlacementBatchSize
        || With.frame < With.configuration.buildingPlacementBatchingStartFrame)) {
        evaluationCount += 1
        val candidate = candidatesFiltered.get(nextEvaluationIndex)
        if (ShowArchitectureHeuristics.inUse) {
          // This does all the math twice! It's slow and only useful for visualizations so we avoid it when possible.
          evaluations(candidate) = EvaluatePlacements.evaluate(blueprint, candidate)
        }
        evaluationValues(candidate) = HeuristicMathMultiplicative.resolve(
          blueprint,
          blueprint.placementProfile.get.weightedHeuristics,
          candidate)
        nextEvaluationIndex += 1
      }
    }
    else {
      // We've evaluated all the tiles! Return our placement conclusions.
      val evaluationValuesMap = evaluationValues.toMap
      val best = EvaluatePlacements.findBest(blueprint, evaluationValuesMap)
      val placement = Placement(
        blueprint,
        best,
        evaluations.values.flatten,
        evaluationValuesMap,
        With.frame)
      With.groundskeeper.updatePlacement(blueprint, placement)
      transition(new PlacementStateReady)
    }
  }
  
  private def stillFiltering  : Boolean = candidatesUnfiltered.exists(nextFilteringIndex < _.length)
  private def stillEvaluating : Boolean = candidatesFiltered.exists(nextEvaluationIndex < _.length)
}
