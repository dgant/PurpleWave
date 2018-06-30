package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Protoss

class Fingerprint2Gate extends FingerprintOr(
  new FingerprintArrivesBy(Protoss.Zealot,    GameTime(3, 40), 2),
  new FingerprintArrivesBy(Protoss.Zealot,    GameTime(4,  15), 3),
  new FingerprintArrivesBy(Protoss.Zealot,    GameTime(5,  0), 5),
  new FingerprintCompleteBy(Protoss.Gateway,  GameTime(2, 55), 2)) {
  
  override val sticky = true
}
