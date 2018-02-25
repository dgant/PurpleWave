package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Protoss

class FingerprintProxyGateway extends FingerprintOr(
  new FingerprintArrivesBy(Protoss.Zealot,    GameTime(3, 15)),
  new FingerprintCompleteBy(Protoss.Gateway,  GameTime(1, 40))) {
  
  trigger = true
}
