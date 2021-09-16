package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class FingerprintNGateways(thresholdGateways: Int) extends Fingerprint {
  override protected def investigate: Boolean = {
    if (With.frame < GameTime(5, 10)()) return false
    val discoveryRatio = 0.8
    var gatewayTimeObserved = With.units.ever.filter(_.isEnemy).filter(_.unitClass.whatBuilds._1 == Protoss.Gateway).map(_.unitClass.buildFrames).sum
    val expectedGatewaySpawns =
      (if (With.fingerprints.twoGate())
        Seq(GameTime(2, 5), GameTime(2, 30), GameTime(4, 30), GameTime(4, 40)).take(thresholdGateways)
      else
        Seq(GameTime(2, 0), GameTime(4, 25), GameTime(4, 30), GameTime(4, 40)).take(thresholdGateways))
        .map(_())
    val gatewayTimeExpected = expectedGatewaySpawns.map(With.framesSince).filter(_ > 0).map(t => t - t % Protoss.Zealot.buildFrames).sum
    val gatewaysExpected    = gatewayTimeObserved.toDouble / gatewayTimeExpected / discoveryRatio
    val output              = gatewaysExpected > thresholdGateways - 0.5
    output
  }

  override protected def lockAfter: Int = GameTime(7, 30)()
}
