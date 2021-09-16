package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Time.GameTime

import scala.collection.SeqView

class FingerprintMannerPylon extends Fingerprint {
  override protected def investigate: Boolean = mannerPylons.nonEmpty
  override def lockAfter: Int = GameTime(5, 0)()
  override val sticky = true
  override def reason: String = f"[${mannerPylons.mkString(", ")}]"
  protected def mannerPylons: SeqView[UnitInfo, Vector[UnitInfo]] = With.geography.ourMain.units.view.filter(u =>
    Protoss.Pylon(u)
    && u.player.isEnemy
    && With.geography.ourMain.harvestingArea.contains(u.tile))
}
