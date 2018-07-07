package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Protoss

class FingerprintDTRush extends FingerprintOr(
  new FingerprintArrivesBy(Protoss.DarkTemplar,       GameTime(7, 0)),
  new FingerprintCompleteBy(Protoss.CitadelOfAdun,    GameTime(6, 0)),
  new FingerprintCompleteBy(Protoss.TemplarArchives,  GameTime(6, 30))) {
  
  override val sticky = true
}
