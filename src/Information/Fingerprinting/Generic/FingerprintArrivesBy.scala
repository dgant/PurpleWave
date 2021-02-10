package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.UnitMatchers.Matcher
import Utilities.GameTime

class FingerprintArrivesBy(
                            unitMatcher : Matcher,
                            gameTime    : GameTime,
                            quantity    : Int = 1)
    extends Fingerprint {
  
  override val sticky = true
  
  override def investigate: Boolean = {
    val targetFrame = gameTime.frames

    // Important performance short-circuit
    if (With.frame > targetFrame) return false

    val units           = With.units.ever.view.filter(u => u.isEnemy && unitMatcher.apply(u))
    val arrivingOnTime  = units.count(_.arrivalFrame() < targetFrame)
    val output          = arrivingOnTime >= quantity
    output
  }
}
