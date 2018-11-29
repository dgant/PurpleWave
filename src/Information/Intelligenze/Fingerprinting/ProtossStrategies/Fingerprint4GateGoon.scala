package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import ProxyBwapi.Races.Protoss

class Fingerprint4GateGoon extends FingerprintOr(
  // Dragoon numbers are reduced slightly to increase sensitivity
  new FingerprintCompleteBy(Protoss.Gateway,  GameTime(6, 0),  4),
  new FingerprintCompleteBy(Protoss.Dragoon,  GameTime(6, 0),  6),  // Actually 7
  new FingerprintCompleteBy(Protoss.Dragoon,  GameTime(6, 30), 8),  // Actually 11
  new FingerprintCompleteBy(Protoss.Dragoon,  GameTime(7, 15), 11), // Actually 15
  new FingerprintArrivesBy(Protoss.Dragoon,   GameTime(6, 45), 6),  // Actually 7
  new FingerprintArrivesBy(Protoss.Dragoon,   GameTime(7, 15), 8),  // Actually 11
  new FingerprintArrivesBy(Protoss.Dragoon,   GameTime(7, 45), 11)) // Actually 15
  {
  
  override val sticky = true
}
