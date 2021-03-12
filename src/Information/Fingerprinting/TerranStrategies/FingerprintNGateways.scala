package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.GameTime

class FingerprintNGateways(thresholdGateways: Double) extends Fingerprint {
  override protected def investigate: Boolean = {
    if (With.frame < GameTime(2, 0)()) return false
    val discoveryRatio  = 1.0
    var gatewayTimeObserved = With.units.ever.filter(_.isEnemy).filter(_.unitClass.whatBuilds._1 == Protoss.Gateway).map(_.unitClass.buildFrames).sum
    val expectedGatewaySpawns =
      (if (With.fingerprints.twoGate())
        Seq(GameTime(2, 5), GameTime(2, 30), GameTime(4, 30), GameTime(4, 40))
      else
        Seq(GameTime(2, 0), GameTime(4, 25), GameTime(4, 30), GameTime(4, 40)))
        .map(_())
    val gatewayTimeExpected = expectedGatewaySpawns.map(With.framesSince).filter(_ > 0).map(t => t - t % Protoss.Zealot.buildFrames).sum
    val gatewaysExpected    = gatewayTimeObserved.toDouble / gatewayTimeExpected / discoveryRatio
    val output              = gatewaysExpected > thresholdGateways - 0.5
    output
  }

  override protected def lockAfter: Int = GameTime(7, 30)()
}
