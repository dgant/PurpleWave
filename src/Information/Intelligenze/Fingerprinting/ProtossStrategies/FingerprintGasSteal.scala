package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Fingerprint
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With

class FingerprintGasSteal extends Fingerprint {
  
  override val sticky = true

  override protected def investigate: Boolean = With.frame < GameTime(5, 0)() && With.geography.ourMain.gas.exists(_.isEnemy)
}
