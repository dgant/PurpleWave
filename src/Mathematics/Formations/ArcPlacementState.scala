package Mathematics.Formations

import Mathematics.Points.Pixel

class ArcPlacementState(arc: Arc, minimumRadius: Double) {
  var radiusPixels    = -1.0
  var angleRadians    = 0.0
  var lastUnitRadians = 0.0
  var nextClockwise   = true
  
  def startRank(radiusDesired: Double) {
    if (radiusDesired > radiusPixels) {
      radiusPixels  = radiusDesired
      recenter()
    }
  }
  
  private def advanceRank() {
    radiusPixels += 32.0
    recenter()
  }
  
  private def recenter() {
    angleRadians       = 0.0
    nextClockwise = true
  }
  
  def reserveSpace(widthPixels: Double): Pixel = {
    if (spaceLeft < widthPixels) {
      advanceRank()
    }
    val unitRadians = widthPixels / radiusPixels
    val output = arc.centerPixel.radiate(
      arc.centerRadians + (angleRadians + unitRadians) * (if (nextClockwise) 1 else -1),
      widthPixels)
    
    nextClockwise = ! nextClockwise
    if (nextClockwise) {
      angleRadians += Math.max(unitRadians, lastUnitRadians)
    }
    lastUnitRadians = unitRadians
    output
  }
  
  def spaceLeft: Double = {
    radiusPixels * 2 * (arc.spanRadians - angleRadians)
  }
}
