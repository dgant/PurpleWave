package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchProxied}
import ProxyBwapi.Races.Protoss

class FingerprintProxyGateway extends FingerprintAnd(
  new FingerprintOr(
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(2, 50)),
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(3, 20), 2),
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(4,  0), 4),
    new FingerprintCompleteBy(
      UnitMatchAnd(Protoss.Gateway, UnitMatchProxied),
      GameTime(5,  0)))) {
  
  override val sticky = true
}
