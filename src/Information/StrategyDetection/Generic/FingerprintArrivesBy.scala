package Information.StrategyDetection.Generic

import Information.StrategyDetection.Fingerprint
import Lifecycle.With
import ProxyBwapi.UnitClass.UnitClass

class FingerprintArrivesBy(
  unitClass : UnitClass,
  gameTime  : GameTime,
  quantity  : Int = 1)
    extends Fingerprint {
  
  var triggered = false
  
  override def matches: Boolean = {
    val wasTriggeredBefore = triggered
    triggered = triggered || With.units.enemy.count(u =>
      u.is(unitClass)
      && With.frame + u.framesToTravelTo(With.geography.home.pixelCenter) < gameTime.frames) >= quantity
    
    triggered
  }
}
