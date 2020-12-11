package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Utilities.GameTime

class FingerprintGasSteal extends Fingerprint {
  
  override val sticky = true

  override protected def investigate: Boolean = With.frame < GameTime(5, 0)() && With.geography.ourMain.gas.exists(_.isEnemy)
}
