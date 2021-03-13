package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Utilities.Minutes

class FingerprintGasSteal extends Fingerprint {

  override protected def investigate: Boolean = {
    With.geography.ourMain.gas.exists(_.isEnemy)
  }

  override def lockAfter: Int = Minutes(5)()

  override val sticky = true
}
