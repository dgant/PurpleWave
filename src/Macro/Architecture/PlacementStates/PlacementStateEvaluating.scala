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
  private var nextFilteringIndex    = 0
  private var nextEvaluationIndex   = 0
  private val evaluationDebugging   = new mutable.HashMap[Tile, Iterable[PlacementHeuristicEvaluation]]
  private val evaluationValues      = new mutable.HashMap[Tile, Double]
  private val evaluationStartFrame  = With.frame
  private var evaluationNanoseconds = 0L
  
  override def step() {
    val nanosecondsOnStart = System.nanoTime()
    if (stillSurveying) {
      val sources = Surveyor.candidates(blueprint)
      candidatesUnfiltered      = Some(new ArrayBuffer[Tile])
      candidatesFiltered        = Some(new ArrayBuffer[Tile])
      candidatesUnfiltered.get  ++= sources.flatMap(_.tiles(blueprint))
      updateStepNanoseconds(nanosecondsOnStart)
    }
    else if (stillFiltering) {
      
      // Filter them (in batches)
      var filterCount = 0
      val filterCountMax = batchSize
      while (stillFiltering && filterCount < filterCountMax) {
        
        val candidate = candidatesUnfiltered.get(nextFilteringIndex)
        if (Architect.canBuild(blueprint, candidate, recheckPathing = true)) {
          candidatesFiltered.get += candidate
        }
        
        filterCount         += 1
        nextFilteringIndex  += 1
      }
      updateStepNanoseconds(nanosecondsOnStart)
    }
    else if (stillEvaluating) {
      // Evaluate them (in batches)
      var evaluationCount = 0
      val evaluationCountMax = batchSize
      while (stillEvaluating && evaluationCount < evaluationCountMax) {
        
        val candidate = candidatesFiltered.get(nextEvaluationIndex)
        if (ShowArchitectureHeuristics.inUse) {
          evaluationDebugging(candidate) = EvaluatePlacements.evaluate(blueprint, candidate)
        }
        
        evaluationValues(candidate) = HeuristicMathMultiplicative.resolve(
          blueprint,
          blueprint.placementProfile.get.weightedHeuristics,
          candidate)
        
        evaluationCount     += 1
        nextEvaluationIndex += 1
      }
      updateStepNanoseconds(nanosecondsOnStart)
    }
    else {
      // We've evaluated all the tiles! Return our placement conclusions.
      val evaluationValuesMap = evaluationValues.toMap
      val best                = EvaluatePlacements.findBest(blueprint, evaluationValuesMap)
      updateStepNanoseconds(nanosecondsOnStart)
      val placement = Placement(
        blueprint,
        best,
        evaluationDebugging.values.flatten,
        evaluationValuesMap,
        totalNanoseconds  = evaluationNanoseconds,
        frameStarted      = evaluationStartFrame,
        frameFinished     = With.frame)
      With.architecture.assumePlacement(placement)
      With.groundskeeper.updatePlacement(blueprint, placement)
      transition(new PlacementStateReady)
    }
  }
  
  private def updateStepNanoseconds(nanosecondsOnStart: Long) {
    val nanosecondsOnEnd = System.nanoTime()
    evaluationNanoseconds += nanosecondsOnEnd - nanosecondsOnStart
  }
  
  private def batchSize: Int =
    if (With.frame < With.configuration.buildingPlacementBatchingStartFrame)
      Int.MaxValue
    else
      With.configuration.buildingPlacementBatchSize
  
  private def stillSurveying  : Boolean = candidatesUnfiltered.isEmpty
  private def stillFiltering  : Boolean = candidatesUnfiltered.exists(nextFilteringIndex < _.length) && candidatesFiltered.size < With.configuration.buildingPlacementMaxTilesToEvaluate
  private def stillEvaluating : Boolean = candidatesFiltered.exists(nextEvaluationIndex < _.length)
}
