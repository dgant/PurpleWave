package Mathematics.Formations.Arcs

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath

class ArcPlacementState(arc: Arc) {
  
  // All measurements in pixels/radius
  
  var currentRadius   : Double  = arc.minRadiusPixels
  var angleDelta      : Double  = 0.0
  var nextClockwise   : Boolean = false
  var lastPlacement   : Pixel   = arc.centerPixel
  var maxWidthPixels  : Double  = 0.0
  
  def startRank(radiusDesired: Double) {
    if (radiusDesired > currentRadius) {
      currentRadius   = radiusDesired
      angleDelta      = 0.0
      nextClockwise   = false
    }
  }
  
  private def advanceToNextRank() {
    startRank(currentRadius + 32.0 * 1.141 + With.configuration.concaveMarginPixels)
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
      arc.centerToArcRadians + angleDelta * (if (nextClockwise) 1 else -1),
      currentRadius)
    
    nextClockwise = ! nextClockwise
    if (nextClockwise) {
      angleDelta += widthPixels / currentRadius
    }
    
    val tile = output.tileIncluding
    if (tile.valid
      && With.grids.walkable.get(tile)
      && ! With.architecture.unwalkable.contains(tile) // Don't concave on reserved tiles
      && (lastPlacement == arc.centerPixel || tile.zone == lastPlacement.zone))
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
