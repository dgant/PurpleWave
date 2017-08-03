package Information.StrategyDetection.Generic

import Information.StrategyDetection.Fingerprint
import Lifecycle.With

object FingerprintScoutedEnemyBase extends Fingerprint {
  override def matches: Boolean = With.geography.enemyBases.exists(_.scouted)
  
}
