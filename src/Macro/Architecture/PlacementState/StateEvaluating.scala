package Macro.Architecture.PlacementState

import Lifecycle.With
import Macro.Architecture.Heuristics.{EvaluatePlacements, PlacementHeuristicEvaluation}
import Macro.Architecture.{Blueprint, Placement, Surveyor}
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

import scala.collection.mutable

class StateEvaluating(blueprint: Blueprint) extends PlacementState {
  
  private var candidates          : Option[Array[Tile]] = None
  private var nextCandidateIndex  : Int                 = 0
  private val evaluations       = new mutable.HashMap[Tile, Iterable[PlacementHeuristicEvaluation]]
  private val evaluationValues  = new mutable.HashMap[Tile, Double]
  
  override def step() {
    if (candidates.isEmpty) {
      // Figure out which tiles to evaluate
      candidates = Some(Surveyor.candidates(blueprint).toArray)
    }
    else if (nextCandidateIndex < candidates.get.length) {
      // Evaluate them (in small batches)
      val candidate = candidates.get(nextCandidateIndex)
      evaluations(candidate) = EvaluatePlacements.evaluate(blueprint, candidate)
      evaluationValues(candidate) = HeuristicMathMultiplicative.resolve(
        blueprint,
        evaluations(candidate),
        candidate)
      nextCandidateIndex += 1
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
    }
  }
}
