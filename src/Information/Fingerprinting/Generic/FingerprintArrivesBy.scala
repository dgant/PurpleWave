package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Utilities.UnitFilters.UnitFilter
import Utilities.Time.FrameCount

class FingerprintArrivesBy(
                            unitMatcher : UnitFilter,
                            gameTime    : FrameCount,
                            quantity    : Int = 1)
    extends Fingerprint {
  
  override val sticky = true
  
  override def investigate: Boolean = {
    val targetFrame = gameTime()

    // Important performance short-circuit
    if (With.frame > targetFrame) return false

    val units           = With.units.everEnemy.filter(unitMatcher)
    val arrivingOnTime  = units.count(_.arrivalFrame() < targetFrame)
    val output          = arrivingOnTime >= quantity
    output
  }
}
