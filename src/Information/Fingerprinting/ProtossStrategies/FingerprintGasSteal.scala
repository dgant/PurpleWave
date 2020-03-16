package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Fingerprint
import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With

class FingerprintGasSteal extends Fingerprint {
  
  override val sticky = true

  override protected def investigate: Boolean = With.frame < GameTime(5, 0)() && With.geography.ourMain.gas.exists(_.isEnemy)
}
