package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Techs.Tech
import Utilities.GameTime

class FingerprintTechBy(tech: Tech, gameTime: GameTime) extends Fingerprint {
  override def investigate: Boolean = With.frame < gameTime() && With.enemies.exists(_.hasTech(tech))
  override val sticky: Boolean = true
}
