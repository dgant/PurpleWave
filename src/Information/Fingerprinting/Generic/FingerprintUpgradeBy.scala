package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Upgrades.Upgrade

class FingerprintUpgradeBy(upgrade: Upgrade, gameTime: GameTime, level: Int = 1) extends Fingerprint {
  override def investigate: Boolean = With.frame < gameTime() && With.enemies.exists(_.getUpgradeLevel(upgrade) >= level)
  override val sticky: Boolean = true
}
