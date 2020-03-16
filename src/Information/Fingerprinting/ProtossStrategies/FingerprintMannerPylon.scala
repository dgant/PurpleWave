package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Fingerprint
import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import ProxyBwapi.Races.Protoss

class FingerprintMannerPylon extends Fingerprint {
  override protected def investigate: Boolean = (
    With.frame < GameTime(5, 0)()
    && With.geography.ourMain.units.exists(u =>
      u.is(Protoss.Pylon)
      && u.player.isEnemy
      && With.geography.ourMain.harvestingArea.contains(u.tileIncludingCenter))
  )

  override val sticky = true
}
