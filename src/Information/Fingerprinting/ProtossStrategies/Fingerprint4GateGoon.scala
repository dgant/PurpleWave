package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.TerranStrategies.FingerprintNGateways
import ProxyBwapi.Races.Protoss
import Utilities.GameTime

class Fingerprint4GateGoon extends FingerprintOr(
  new FingerprintNGateways(4),
  new FingerprintAnd(
    new FingerprintCompleteBy(Protoss.Assimilator,      GameTime(6, 0)),
    new FingerprintCompleteBy(Protoss.CyberneticsCore,  GameTime(6, 0)),
    new FingerprintCompleteBy(Protoss.Gateway,          GameTime(6, 0),  4)),
  // Dragoon numbers are reduced slightly to increase sensitivity
  //new FingerprintCompleteBy(Protoss.Dragoon,  GameTime(6, 0),  6),  // Actually 7
  new FingerprintCompleteBy(Protoss.Dragoon,  GameTime(6, 30), 10),  // Actually 11
  new FingerprintCompleteBy(Protoss.Dragoon,  GameTime(7, 15), 14), // Actually 15
  new FingerprintCompleteBy(Protoss.Dragoon,  GameTime(7, 45), 17),
  //new FingerprintArrivesBy(Protoss.Dragoon,   GameTime(6, 45), 6),  // Actually 7
  new FingerprintArrivesBy(Protoss.Dragoon,   GameTime(7, 15), 10),  // Actually 11
  new FingerprintArrivesBy(Protoss.Dragoon,   GameTime(7, 45), 14), // Actually 15
  new FingerprintArrivesBy(Protoss.Dragoon,   GameTime(8, 15), 17))
  {
  override val sticky = true
}
