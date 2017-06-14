package Micro.Heuristics.Movement

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Micro.Task.ExecutionState
import ProxyBwapi.UnitInfo.UnitInfo

object EvaluatePixels {

  def best(state:ExecutionState, profile:MovementProfile): Pixel = {
    
    val candidates = getCandidates(state)
  
    if (candidates.isEmpty) {
      return state.unit.pixelCenter
    }
    
    state.movementHeuristicResults =
      candidates.flatten(candidate =>
        profile.weightedHeuristics.map(weightedHeuristic =>
          new MovementHeuristicResult(
            weightedHeuristic.heuristic,
            state,
            candidate,
            weightedHeuristic.weighMultiplicatively(state, candidate),
            weightedHeuristic.color)))
    
    HeuristicMathMultiplicative.best(state, profile.weightedHeuristics, candidates)
  }
  
  def getCandidates(state:ExecutionState):Vector[Pixel] = {

    val startingPixel = state.unit.pixelCenter
    val startingZone  = startingPixel.zone
    val otherPixels =
      (0 until 256 by With.configuration.performanceMicroAngleStep)
        .flatten(angle => {
          val targetPixel = startingPixel.radiateDegrees(angle, 50.0)
          if (acceptable(state.unit, targetPixel, startingPixel, startingZone))
            Some(targetPixel)
          else
            None
        })
    
    Vector(startingPixel) ++ otherPixels
  }
  
  def acceptable(
    unit:UnitInfo,
    candidate:Pixel,
    startingPixel:Pixel,
    startingZone:Zone)
      :Boolean = {
    
    if ( ! candidate.valid) return false
    if (unit.flying) return true
    if ( ! With.grids.walkable.get(candidate.tileIncluding)) return false
    
    val candidateZone = candidate.zone
    if (startingZone == candidateZone) return true
    
    startingZone.edges.exists(
      edge => edge.zones.exists(_ == candidateZone)
      &&
        candidate.pixelDistanceFast(startingPixel) * 2 >
        candidate.pixelDistanceFast(edge.centerPixel) + startingPixel.pixelDistanceFast(edge.centerPixel))
  }
  
}
