package Micro.Heuristics.Movement

import Debugging.Visualizations.Views.Micro.ShowMovementHeuristics
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.UnitInfo

object EvaluatePixels {

  def best(state: ActionState, profile: MovementProfile): Pixel = {
    
    val candidates = getCandidates(state)
  
    if (candidates.isEmpty) {
      return state.unit.pixelCenter
    }
    
    if (ShowMovementHeuristics.inUse) {
      state.movementHeuristicResults =
        candidates.flatten(candidate =>
          profile.weightedHeuristics.map(weightedHeuristic =>
            new MovementHeuristicEvaluation(
              weightedHeuristic.heuristic,
              state,
              candidate,
              weightedHeuristic.weighMultiplicatively(state, candidate),
              weightedHeuristic.color)))
    }
    
    HeuristicMathMultiplicative.best(state, profile.weightedHeuristics, candidates)
  }
  
  def getCandidates(state: ActionState): Vector[Pixel] = {

    val startingPixel = state.unit.pixelCenter
    val startingZone  = startingPixel.zone
    val otherPixels =
      (0 until 256 by With.configuration.performanceMicroAngleStep)
        .flatten(angle => {
          // Jaj22 puts the cutoff for straight line movement at 79/80 pixels.
          // Tscmoo puts it at 64 and below.
          // Put it a little under that threshold to account for changed positions due to latency.
          // Jaj22: "Behaviour changes drastically at 64x + 16 pixel boundaries."
          // Tscmoo2: if you right click for a unit within 64 pixels, it'll just move directly
          // Tscmoo2: for longer distances, it has to go through a waypoint kinda
          // TODO: Just pick the pixels we want and then consider pathing in the Commander.
          val targetPixel = startingPixel.radiateDegrees(angle, 70.0)
          if (acceptable(state.unit, targetPixel, startingPixel, startingZone))
            Some(targetPixel)
          else
            None
        })
    
    Vector(startingPixel) ++ otherPixels
  }
  
  def acceptable(
    unit          : UnitInfo,
    candidate     : Pixel,
    startingPixel :Pixel,
    startingZone  : Zone)
      : Boolean = {
    
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
