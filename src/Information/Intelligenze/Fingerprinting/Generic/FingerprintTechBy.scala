package Information.Intelligenze.Fingerprinting.Generic

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Techs.Tech

class FingerprintTechBy(tech: Tech, gameTime: GameTime) extends Fingerprint {
  override def investigate: Boolean = With.frame < gameTime() && With.enemies.exists(_.hasTech(tech))
  override val sticky: Boolean = true
}
