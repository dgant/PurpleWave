package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import ProxyBwapi.Races.Protoss

class Fingerprint4GateGoon extends FingerprintOr(
  // Dragoon numbers are reduced slightly to increase sensitivity
  new FingerprintCompleteBy(Protoss.Gateway,  GameTime(6, 0),  4),
  new FingerprintCompleteBy(Protoss.Dragoon,  GameTime(5, 45), 6),  // Actually 7
  new FingerprintCompleteBy(Protoss.Dragoon,  GameTime(6, 15), 8),  // Actually 11
  new FingerprintCompleteBy(Protoss.Dragoon,  GameTime(7,  0), 11), // Actually 15
  new FingerprintArrivesBy(Protoss.Dragoon,   GameTime(6, 30), 6),  // Actually 7
  new FingerprintArrivesBy(Protoss.Dragoon,   GameTime(7,  0), 8),  // Actually 11
  new FingerprintArrivesBy(Protoss.Dragoon,   GameTime(7, 30), 11)) // Actually 15
  {
  
  override val sticky = true
}
