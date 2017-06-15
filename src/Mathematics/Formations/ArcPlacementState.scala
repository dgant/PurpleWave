package Mathematics.Formations

import Mathematics.Points.Pixel

class ArcPlacementState(arc: Arc, minimumRadius: Double) {
  var radiusPixels    = arc.minRadiusPixels
  var angleRadians    = 0.0
  var lastUnitRadians = 0.0
  var spanPixels      = 0.0
  var spanPixelsLeft  = 0.0
  var nextClockwise   = true
  
  def startRank(radiusDesired: Double) {
    if (radiusDesired > radiusPixels) {
      radiusPixels    = radiusDesired
      spanPixels      = arc.spanRadians * radiusPixels
      spanPixelsLeft  = spanPixels
      angleRadians    = 0.0
      nextClockwise   = true
    }
  }
  
  private def advanceToNextRank() {
    startRank(radiusPixels + 32.0)
  }
  
  def reserveSpace(widthPixels: Double): Pixel = {
    if (spanPixelsLeft < widthPixels) {
      advanceToNextRank()
    }
    val unitRadians = widthPixels / radiusPixels
    val output = arc.centerPixel.radiateRadians(
      arc.centerAngleRadians + (angleRadians + unitRadians) * (if (nextClockwise) 1 else -1),
      widthPixels)
    
    nextClockwise = ! nextClockwise
    if (nextClockwise) {
      angleRadians += Math.max(unitRadians, lastUnitRadians)
    }
    lastUnitRadians = unitRadians
    output
  }
}
