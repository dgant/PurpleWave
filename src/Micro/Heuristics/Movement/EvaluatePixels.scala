package Micro.Heuristics.Movement

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Pixels.Pixel
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object EvaluatePixels {

  def best(intent:Intention, profile:MovementProfile): Pixel = {
    
    val candidates = getCandidates(intent)
  
    if (candidates.isEmpty) {
      return intent.unit.pixelCenter
    }
    
    if (With.configuration.visualizeHeuristicMovement) {
      intent.state.movementHeuristicResults =
        candidates.flatten(candidate =>
          profile.weightedHeuristics.map(weightedHeuristic =>
            new MovementHeuristicResult(
              weightedHeuristic.heuristic,
              intent,
              candidate,
              weightedHeuristic.weighMultiplicatively(intent, candidate),
              weightedHeuristic.color)))
    }
    
    HeuristicMathMultiplicative.best(intent, profile.weightedHeuristics, candidates)
  }
  
  def getCandidates(intent:Intention):Vector[Pixel] = {

    val startingPixel = intent.unit.pixelCenter
    val startingZone  = startingPixel.zone
    val otherPixels =
      (0 until 256 by 2)
        .flatten(angle => {
          val targetPixel = startingPixel.radiate(angle, 55.0)
          if (acceptable(intent.unit, targetPixel, startingPixel, startingZone))
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
