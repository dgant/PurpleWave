package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import Planning.Composition.UnitMatchers.UnitMatchProxied
import ProxyBwapi.Races.Protoss

class FingerprintProxyGateway extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.cannonRush),
  new FingerprintOr(
    new FingerprintArrivesBy(Protoss.Zealot,    GameTime(2, 50)),
    new FingerprintCompleteBy(UnitMatchProxied,  GameTime(4,  0)))) {
  
  override val sticky = true
}
