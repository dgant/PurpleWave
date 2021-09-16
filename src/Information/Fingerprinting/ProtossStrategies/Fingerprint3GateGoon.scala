package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.TerranStrategies.FingerprintNGateways
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class Fingerprint3GateGoon extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.fourGateGoon),
  new FingerprintOr(
    new FingerprintNGateways(3),
    new FingerprintAnd(
      new FingerprintCompleteBy(Protoss.Assimilator,      GameTime(6, 0)),
      new FingerprintCompleteBy(Protoss.CyberneticsCore,  GameTime(6, 0)),
      new FingerprintCompleteBy(Protoss.Gateway,          GameTime(6, 0), 3))))