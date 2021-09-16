package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Time.FrameCount

class FingerprintGasEmptyUntil(time: FrameCount) extends Fingerprint {
  override protected def investigate: Boolean = matchingGas.isEmpty
  override def sticky: Boolean = With.scouting.enemyMain.isDefined && With.frame >= time()
  override def reason: String = f"Gasses: [${matchingGas.mkString(", ")}]"
  protected def matchingGas: Iterable[UnitInfo] = With.scouting.enemyMain.toVector.flatMap(_.gas.filter(gas => gas.player.isEnemy && gas.lastSeen < time()))
}
