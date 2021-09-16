package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class Fingerprint1GateCore extends FingerprintOr(
  With.fingerprints.fourGateGoon,
  new FingerprintAnd(
    new FingerprintNot(With.fingerprints.gasSteal),
    new FingerprintCompleteBy(Protoss.Assimilator,    GameTime(2, 45))),
  new FingerprintCompleteBy(Protoss.CyberneticsCore,  GameTime(3, 30)),
  new FingerprintArrivesBy(Protoss.Dragoon,           GameTime(4, 40)),
  new FingerprintArrivesBy(Protoss.DarkTemplar,       GameTime(6, 0)),
  new FingerprintArrivesBy(Protoss.Observer,          GameTime(6, 30)),
  new FingerprintArrivesBy(Protoss.Reaver,            GameTime(7, 0)),
  new FingerprintArrivesBy(Protoss.Shuttle,           GameTime(7, 0))) {
  
  override val sticky = true
}
