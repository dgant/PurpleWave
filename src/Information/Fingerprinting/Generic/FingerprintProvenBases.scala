package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Utilities.Time.Hours

class FingerprintProvenBases(count: Int, by: Int = Hours(2)()) extends Fingerprint {
  override val sticky = true
  override def investigate: Boolean = With.frame < by && With.geography.enemyBases.count(b => b.townHall.isDefined || b.isStartLocation) >= count
}
