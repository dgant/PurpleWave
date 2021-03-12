package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.GameTime

class FingerprintNFactories(thresholdFactories: Double) extends Fingerprint {
  override protected def investigate: Boolean = {
    if (With.frame < GameTime(5, 20)()) return false
    val discoveryRatio      = 1.0
    var factoryTimeObserved = With.units.ever.filter(_.isEnemy).filter(_.unitClass.whatBuilds._1 == Terran.Factory).map(_.unitClass.buildFrames).sum
    val machineShopExists   = With.enemies.exists(_.hasUpgrade(Terran.VultureSpeed)) || With.unitsShown.any(Terran.MachineShop, Terran.SpiderMine, Terran.SiegeTankUnsieged, Terran.SiegeTankSieged)
    val machineShopTime     = if (machineShopExists) Terran.MachineShop.buildFrames else 0
    val expectedFactoryTime = With.framesSince(GameTime(3, 30)() + machineShopTime)
    val expectedFactories   = factoryTimeObserved.toDouble / expectedFactoryTime / discoveryRatio
    val output              = expectedFactories > thresholdFactories - 0.5
    output
  }

  override protected def lockAfter: Int = GameTime(8, 0)()
}
