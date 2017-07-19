package Micro.Heuristics.Movement

import Debugging.Visualizations.Views.Micro.ShowMovementHeuristics
import Lifecycle.With
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Mathematics.Shapes.Circle
import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.UnitInfo

object EvaluatePixels {

  def best(state: ActionState, profile: MovementProfile): Pixel = {
    
    val candidates = getSprayCandidates(state)
  
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
  
  def getSprayCandidates(state: ActionState): Vector[Pixel] = {
    val startingPixel = state.unit.pixelCenter
    val startingZone  = startingPixel.zone
    val scalingFactor = 8
    val pixels =
      Circle.points(12)
        .map(point => state.unit.pixelCenter.add(scalingFactor * point.x, scalingFactor * point.y))
        .filter(pixel => acceptable(state.unit, pixel, startingPixel))
    pixels
  }
  
  def getRadialCandidates(state: ActionState): Vector[Pixel] = {

    val startingPixel = state.unit.pixelCenter
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
          val targetPixel = startingPixel.radiate256Degrees(angle, 70.0)
          if (acceptable(state.unit, targetPixel, startingPixel))
            Some(targetPixel)
          else
            None
        })
    
    Vector(startingPixel) ++ otherPixels
  }
  
  def acceptable(
    unit          : UnitInfo,
    candidate     : Pixel,
    startingPixel : Pixel)
      : Boolean = {
    
    if ( ! candidate.valid)                                   return false
    if (unit.flying)                                          return true
    if ( ! With.grids.walkable.get(candidate.tileIncluding))  return false
    true
  }
  
}
