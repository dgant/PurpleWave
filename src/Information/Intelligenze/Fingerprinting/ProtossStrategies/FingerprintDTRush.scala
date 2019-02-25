package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Fingerprint
import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Protoss

class FingerprintDTRush extends FingerprintOr(
  new FingerprintArrivesBy(Protoss.DarkTemplar, GameTime(7, 0)),
  new FingerprintAnd(
    new FingerprintCompleteBy(Protoss.CitadelOfAdun, GameTime(6, 0)),
    new Fingerprint {
      override protected def investigate: Boolean = (
        ! With.units.enemy.exists(u => u.is(Protoss.CyberneticsCore) && u.upgrading)
        && ! With.enemy.hasUpgrade(Protoss.DragoonRange))
    }),
  new FingerprintCompleteBy(Protoss.TemplarArchives, GameTime(6, 30))) {
  
  override val sticky = true
}
