package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Protoss

class FingerprintDTRush extends FingerprintOr(
  new FingerprintArrivesBy(Protoss.DarkTemplar, GameTime(7, 15)),
  new FingerprintCompleteBy(Protoss.TemplarArchives, GameTime(6, 45)),
  new FingerprintAnd(
    new FingerprintCompleteBy(Protoss.CitadelOfAdun, GameTime(6, 15)),
    new FingerprintNot(With.fingerprints.dragoonRange),
    new FingerprintNot(With.fingerprints.fourGateGoon))) {
  
  override val sticky = true
}
