package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import ProxyBwapi.Races.Protoss

class Fingerprint4GateGoon extends FingerprintOr(
  new FingerprintAnd(
    new FingerprintCompleteBy(Protoss.Assimilator,      GameTime(6, 0)),
    new FingerprintCompleteBy(Protoss.CyberneticsCore,  GameTime(6, 0)),
    new FingerprintCompleteBy(Protoss.Gateway,          GameTime(6, 0),  4)),
  // Dragoon numbers are reduced slightly to increase sensitivity
  new FingerprintCompleteBy(Protoss.Dragoon,  GameTime(6, 0),  6),  // Actually 7
  new FingerprintCompleteBy(Protoss.Dragoon,  GameTime(6, 30), 9),  // Actually 11
  new FingerprintCompleteBy(Protoss.Dragoon,  GameTime(7, 15), 13), // Actually 15
  new FingerprintCompleteBy(Protoss.Dragoon,  GameTime(7, 45), 15),
  new FingerprintArrivesBy(Protoss.Dragoon,   GameTime(6, 45), 6),  // Actually 7
  new FingerprintArrivesBy(Protoss.Dragoon,   GameTime(7, 15), 9),  // Actually 11
  new FingerprintArrivesBy(Protoss.Dragoon,   GameTime(7, 45), 13), // Actually 15
  new FingerprintArrivesBy(Protoss.Dragoon,   GameTime(8, 15), 15))
  {
  override val sticky = true
}
