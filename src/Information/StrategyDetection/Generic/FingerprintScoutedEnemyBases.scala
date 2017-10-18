package Information.StrategyDetection.Generic

import Information.StrategyDetection.Fingerprint
import Lifecycle.With

class FingerprintScoutedEnemyBases(count: Int) extends Fingerprint {
  override def matches: Boolean = With.geography.enemyBases.count(_.scouted) >= count
  
}
