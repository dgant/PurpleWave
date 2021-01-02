package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.GameTime

class FingerprintNFactories(thresholdFactories: Double) extends Fingerprint {
  override protected def investigate: Boolean = {
    if (With.frame < GameTime(5, 20)()) {
      return false
    }
    if (With.frame > GameTime(8, 0)()) {
      return matched
    }
    var factoryUnitTime: Int = 0
    With.units.ever.foreach(u => if (u.isEnemy && u.unitClass.whatBuilds._1 == Terran.Factory) {
      factoryUnitTime += u.unitClass.buildFrames
    })
    val discoveryRatio      = 1.0
    val expectedFactoryTime = With.framesSince(GameTime(4, 40)())
    val expectedFactories   = factoryUnitTime.toDouble / expectedFactoryTime / discoveryRatio
    val output              = expectedFactories > thresholdFactories - 0.5
    output
  }

  override protected def lockAfter: Int = GameTime(9, 0)()
}
