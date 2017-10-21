package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.{Protoss, Zerg}

class FingerprintProxyGateway extends FingerprintOr(
  new FingerprintArrivesBy(Protoss.Zealot,      GameTime(3, 15)),
  new FingerprintCompleteBy(Zerg.SpawningPool,  GameTime(1, 40))) {
  
  trigger = true
}
