package Information.Intelligenze.Fingerprinting.Generic

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With

class FingerprintScoutedEnemyBases(count: Int, by: GameTime = GameTime(120, 0)) extends Fingerprint {
  
  override val sticky = true
  override def investigate: Boolean = With.geography.enemyBases.count(b => b.scouted && b.townHall.isDefined) >= count && With.frame < by()
  
}
