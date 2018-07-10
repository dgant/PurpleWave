package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchProxied}
import ProxyBwapi.Races.Protoss

class FingerprintProxyGateway extends FingerprintAnd(
  new FingerprintOr(
    new FingerprintArrivesBy(
      Protoss.Zealot,
      GameTime(2, 50)),
    new FingerprintCompleteBy(
      UnitMatchAnd(Protoss.Gateway, UnitMatchProxied),
      GameTime(4,  0)))) {
  
  override val sticky = true
}
