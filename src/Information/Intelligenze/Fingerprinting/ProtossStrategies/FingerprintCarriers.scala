package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Protoss

class FingerprintCarriers extends FingerprintOr(
  new FingerprintArrivesBy(Protoss.Zealot,    GameTime(3, 30), 2),
  new FingerprintArrivesBy(Protoss.Zealot,    GameTime(3, 55), 3),
  new FingerprintArrivesBy(Protoss.Zealot,    GameTime(4, 20), 4),
  new FingerprintCompleteBy(Protoss.Gateway,  GameTime(2, 55), 2)) {
  
  override val sticky = true
}
