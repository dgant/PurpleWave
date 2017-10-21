package Information.Intelligence.Fingerprinting.ProtossStrategies

import Information.Intelligence.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Protoss

class Fingerprint2Gate extends FingerprintOr(
  new FingerprintArrivesBy(Protoss.Zealot,    GameTime(4, 0), 2),
  new FingerprintCompleteBy(Protoss.Gateway,  GameTime(2, 30))) {

  trigger = true
}
