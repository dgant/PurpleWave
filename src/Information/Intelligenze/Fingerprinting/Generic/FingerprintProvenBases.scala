package Information.Intelligenze.Fingerprinting.Generic

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With

class FingerprintProvenBases(count: Int, by: GameTime = GameTime(120, 0)) extends Fingerprint {
  
  override val sticky = true
  override def investigate: Boolean = With.frame < by() && With.geography.enemyBases.count(b => b.townHall.isDefined || b.isStartLocation) >= count
}
