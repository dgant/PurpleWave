package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.Orders

class FingerprintDragoonRange extends Fingerprint {
  override protected def investigate: Boolean = (
    With.enemies.exists(_.hasUpgrade(Protoss.DragoonRange))
    || With.units.existsEnemy(u => u.is(Protoss.CyberneticsCore) && (u.upgrading || u.order == Orders.Upgrade))
  )

  override val sticky: Boolean = With.enemies.exists(_.hasUpgrade(Protoss.DragoonRange))
}
