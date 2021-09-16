package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{Orders, UnitInfo}

class FingerprintDragoonRange extends Fingerprint {
  override protected def investigate: Boolean = enemyHasRange || enemySpinningCore.isDefined
  override val sticky: Boolean = With.enemies.exists(_.hasUpgrade(Protoss.DragoonRange))
  override protected def reason: String = if (enemyHasRange) "Enemy has range" else enemySpinningCore.mkString("")
  protected def enemyHasRange: Boolean = With.enemies.exists(_.hasUpgrade(Protoss.DragoonRange))
  protected def enemySpinningCore: Option[UnitInfo] = With.units.enemy.find(u => u.is(Protoss.CyberneticsCore) && (u.upgrading || u.order == Orders.Upgrade))
}
