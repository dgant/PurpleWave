package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.TerranStrategies.FingerprintNGateways
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class Fingerprint2GateGoon extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.twoGate),
  new FingerprintNot(With.fingerprints.threeGateGoon),
  new FingerprintNot(With.fingerprints.fourGateGoon),
  new FingerprintOr(
    new FingerprintAnd(
      new FingerprintNGateways(2),
      new FingerprintOr(
        With.fingerprints.dragoonRange,
        With.fingerprints.oneGateCore)),
    new FingerprintAnd(
      new FingerprintCompleteBy(Protoss.Assimilator,      GameTime(6, 0)),
      new FingerprintCompleteBy(Protoss.CyberneticsCore,  GameTime(6, 0)),
      new FingerprintCompleteBy(Protoss.Gateway,          GameTime(6, 0), 2))))