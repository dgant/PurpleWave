package Information.Intelligenze.Fingerprinting.Generic

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.UnitMatchers.UnitMatcher

class FingerprintArrivesBy(
  unitMatcher : UnitMatcher,
  gameTime    : GameTime,
  quantity    : Int = 1)
    extends Fingerprint {
  
  override val sticky = true
  
  override def investigate: Boolean = {
    val units           = With.units.ever.view.filter(u => u.isEnemy && unitMatcher.accept(u))
    val targetFrame     = gameTime.frames
    val arrivingOnTime  = units.count(_.arrivalFrame() < targetFrame)
    val output          = arrivingOnTime >= quantity
    output
  }
}
