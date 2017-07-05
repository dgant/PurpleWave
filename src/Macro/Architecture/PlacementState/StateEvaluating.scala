package Macro.Architecture.PlacementState

import Lifecycle.With
import Macro.Architecture.Heuristics.{EvaluatePlacements, PlacementHeuristicEvaluation}
import Macro.Architecture.{Architect, Blueprint, Placement, Surveyor}
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class StateEvaluating(blueprint: Blueprint) extends PlacementState {
  
  private var candidatesUnfiltered  : Option[Array[Tile]]       = None
  private var candidatesFiltered    : Option[ArrayBuffer[Tile]] = None
  private var nextFilteringIndex    : Int                       = 0
  private var nextEvaluationIndex   : Int                       = 0
  private val evaluations       = new mutable.HashMap[Tile, Iterable[PlacementHeuristicEvaluation]]
  private val evaluationValues  = new mutable.HashMap[Tile, Double]
  
  override def step() {
    if (candidatesUnfiltered.isEmpty) {
      // Figure out which tiles to evaluate
      candidatesUnfiltered  = Some(Surveyor.candidates(blueprint).toArray)
      candidatesFiltered    = Some(new ArrayBuffer[Tile])
    }
    else if (stillFiltering) {
      // Filter them (in batches)
      var evaluationCount = 0
      while (stillFiltering && evaluationCount < With.configuration.buildingPlacementBatchSize) {
        evaluationCount += 1
        val nextCandidate = candidatesUnfiltered.get(nextFilteringIndex)
        if (Architect.canBuild(blueprint, nextCandidate)) {
          candidatesFiltered.get += nextCandidate
        }
        nextFilteringIndex += 1
      }
    }
    else if (stillEvaluating) {
      // Evaluate them (in batches)
      var evaluationCount = 0
      while (stillEvaluating && evaluationCount < With.configuration.buildingPlacementBatchSize) {
        evaluationCount += 1
        val candidate = candidatesFiltered.get(nextEvaluationIndex)
        if (With.visualization.enabled) {
          evaluations(candidate) = EvaluatePlacements.evaluate(blueprint, candidate)
        }
        evaluationValues(candidate) = HeuristicMathMultiplicative.resolve(
          blueprint,
          blueprint.placement.weightedHeuristics,
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
      transition(new StateReady)
    }
  }
  
  private def stillFiltering  : Boolean = candidatesUnfiltered.exists(nextFilteringIndex < _.length)
  private def stillEvaluating : Boolean = candidatesFiltered.exists(nextEvaluationIndex < _.length)
}
