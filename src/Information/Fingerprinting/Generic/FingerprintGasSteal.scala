package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Time.Minutes

class FingerprintGasSteal extends Fingerprint {
  override protected def investigate: Boolean = stolenGas.nonEmpty
  override def lockAfter: Int = Minutes(5)()
  override def reason: String = f"Stolen: [${stolenGas.mkString(", ")}]"
  override val sticky = true
  protected def stolenGas: Vector[UnitInfo] = With.geography.ourMain.gas.filter(_.isEnemy)
}
