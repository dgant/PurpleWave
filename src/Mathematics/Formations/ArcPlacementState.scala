package Mathematics.Formations

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath

class ArcPlacementState(arc: Arc, minimumRadius: Double) {
  
  // All measurements in pixels/radius
  
  var currentRadius = arc.minRadiusPixels
  var angleDelta    = 0.0
  var nextClockwise = false
  var lastPlacement = arc.centerPixel
  
  def startRank(radiusDesired: Double) {
    if (radiusDesired > currentRadius) {
      currentRadius   = radiusDesired
      angleDelta      = 0.0
      nextClockwise   = false
    }
  }
  
  private def advanceToNextRank() {
    startRank(currentRadius + 32.0)
  }
  
  def reserveSpace(widthPixels: Double): Pixel = {
    var output: Option[Pixel] = None
    var attempts = 0
    while (attempts < 10 && output.isEmpty) {
      attempts += 1
      output = tryReserveSpace(widthPixels)
    }
    lastPlacement = output.getOrElse(lastPlacement)
    lastPlacement
  }
  
  private def tryReserveSpace(widthPixels: Double): Option[Pixel] = {
    if (arcPixelsLeft < widthPixels) {
      advanceToNextRank()
    }
    
    val output = arc.centerPixel.radiateRadians(
      arc.centerAngleRadians + angleDelta * (if (nextClockwise) 1 else -1),
      currentRadius)
    
    nextClockwise = ! nextClockwise
    if (nextClockwise) {
      angleDelta += widthPixels / currentRadius
    }
    
    if (With.grids.walkable.get(output.tileIncluding))
      Some(output)
    else
      None
  }
  
  def arcPixelsTotal: Double = {
    currentRadius * arc.spanRadians / PurpleMath.twoPI
  }
  
  def arcPixelsLeft: Double = {
    arcPixelsTotal * (arc.spanRadians - 2.0 * angleDelta)
  }
}
