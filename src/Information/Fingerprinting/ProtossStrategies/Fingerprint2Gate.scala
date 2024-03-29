package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class Fingerprint2Gate extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.oneGateCore),
  new FingerprintOr(
    new FingerprintArrivesBy(Protoss.Zealot,    GameTime(3, 30), 2),
    new FingerprintArrivesBy(Protoss.Zealot,    GameTime(3, 55), 3),
    new FingerprintArrivesBy(Protoss.Zealot,    GameTime(4, 20), 4),

    new FingerprintCompleteBy(Protoss.Zealot,   GameTime(3, 10), 3),
    new FingerprintCompleteBy(Protoss.Zealot,   GameTime(3, 35), 4),
    new FingerprintCompleteBy(Protoss.Zealot,   GameTime(4,  0), 5),

    new FingerprintCompleteBy(Protoss.Gateway,  GameTime(2, 55), 2))) {

  override val sticky: Boolean = With.frame > GameTime(4, 30)()
}
