package Information.StrategyDetection.Generic

import Information.StrategyDetection.Fingerprint
import Lifecycle.With

class FingerprintScoutedEnemyBase extends Fingerprint {
  override def matches: Boolean = With.geography.enemyBases.exists(_.scouted)
  
}
