package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import ProxyBwapi.Races.Protoss

class FingerprintGatewayFirst extends FingerprintOr(
  new FingerprintCompleteBy(Protoss.Gateway,  GameTime(2, 40)),
  new FingerprintArrivesBy(Protoss.Zealot,    GameTime(3, 45))) {
  
  override val sticky = true
}
