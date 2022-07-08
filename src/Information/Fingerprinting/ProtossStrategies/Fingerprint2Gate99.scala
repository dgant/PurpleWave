package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class Fingerprint2Gate99 extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.oneGateCore),
  new FingerprintOr(
    new FingerprintArrivesBy(Protoss.Zealot,    GameTime(3, 15), 2),
    new FingerprintArrivesBy(Protoss.Zealot,    GameTime(3, 20), 3),
    new FingerprintArrivesBy(Protoss.Zealot,    GameTime(3, 30), 4),
    new FingerprintArrivesBy(Protoss.Zealot,    GameTime(3, 35), 5),

    new FingerprintCompleteBy(Protoss.Zealot,   GameTime(2, 45), 2),
    new FingerprintCompleteBy(Protoss.Zealot,   GameTime(2, 50), 3),
    new FingerprintCompleteBy(Protoss.Zealot,   GameTime(3, 10), 4),
    new FingerprintCompleteBy(Protoss.Zealot,   GameTime(3, 15), 5),

    new FingerprintCompleteBy(Protoss.Gateway,  GameTime(2, 20), 2))) {

  override val sticky: Boolean = With.frame > GameTime(4, 30)()
}
