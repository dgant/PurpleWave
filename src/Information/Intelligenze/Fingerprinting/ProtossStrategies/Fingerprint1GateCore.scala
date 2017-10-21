package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Protoss

class Fingerprint1GateCore extends FingerprintAnd(
  new FingerprintScoutedEnemyBases(1),
  new FingerprintNot(With.intelligence.fingerprints.fingerprint2Gate),
  new FingerprintOr(
    new FingerprintCompleteBy(Protoss.CyberneticsCore,  GameTime(4, 0)),
    new FingerprintCompleteBy(Protoss.Dragoon,          GameTime(5, 0)))) {
  
  trigger = true
}
