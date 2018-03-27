package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Protoss

class FingerprintProxyGateway extends FingerprintOr(
  new FingerprintArrivesBy(Protoss.Zealot,    GameTime(2, 50)),
  new FingerprintCompleteBy(Protoss.Gateway,  GameTime(2,  5))) {
  
  override val sticky = true
}
