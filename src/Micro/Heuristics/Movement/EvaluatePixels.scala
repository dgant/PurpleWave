package Micro.Heuristics.Movement

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Heuristics.HeuristicMath
import Mathematics.Pixels.{Pixel, Point}
import Micro.Heuristics.MovementHeuristics.MovementHeuristicResult
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object EvaluatePixels {
  
  val points = Vector(
    new Point(-1, -2),
    new Point( 0, -2),
    new Point( 1, -2),
    new Point(-2, -1),
    new Point(-1, -1),
    new Point( 0, -1),
    new Point( 1, -1),
    new Point( 2, -1),
    new Point(-2,  0),
    new Point(-1,  0),
    new Point( 0,  0),
    new Point( 1,  0),
    new Point( 2,  0),
    new Point(-2,  1),
    new Point(-1,  1),
    new Point( 0,  1),
    new Point( 1,  1),
    new Point( 2,  1),
    new Point(-1,  2),
    new Point( 0,  2),
    new Point( 1,  2)
  )
  
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
              weightedHeuristic.weigh(intent, candidate),
              weightedHeuristic.color)))
    }
    
    HeuristicMath.best(intent, profile.weightedHeuristics, candidates)
  }
  
  def getCandidates(intent:Intention):Vector[Pixel] = {
    
    // According to JohnJ's research, there are two interesting thresholds for length of move orders:
    //
    // 63 pixels:       At and below this threshold, units move in straight lines in any of 256 directions.
    //                  Above that threshold, units use BW pathing which produces segments using only 8 directions
    //
    // 64N + 16 pixels: Units moving short distances don't use their full acceleration.
    //                  He noted particularly fast movement at 80 pixels and 144 pixels.
    //
    // We want to make use of these quirks to optimize our movement.
    
    // So, consider the following pixels:
    // + The starting pixel
    // + The pixels in 8 orthogonal directions at 144, 80, 63 pixels (as far as we can go in the same zone)
    // + Pixels in other directions up to 63 pixels away
    //
  
    val startingPixel = intent.unit.project(With.latency.framesRemaining)
    val startingZone  = startingPixel.zone
    val otherPixels =
      (0 until 256 by 4)
        .flatten(angle => {
          val targetPixel = startingPixel.radiate(angle, 32.0)
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
