package Information.Intelligenze.Fingerprinting.Generic

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Mathematics.PurpleMath
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitInfo.UnitInfo

class FingerprintArrivesBy(
  unitMatcher : UnitMatcher,
  gameTime    : GameTime,
  quantity    : Int = 1)
    extends Fingerprint {
  
  override val sticky = true
  
  override def investigate: Boolean = {
    val units           = With.units.ever.filter(u => u.isEnemy && unitMatcher.accept(u))
    val targetFrame     = gameTime.frames
    val arrivalTimes    = units.map(u => (u, u.arrivalFrame())).toMap
    val arrivingOnTime  = arrivalTimes.count(_._2 < targetFrame)
    val output          = arrivingOnTime >= quantity
    output
  }
}
