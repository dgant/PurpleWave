package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Races.Protoss

class FingerprintCannonRush extends Fingerprint {
  override protected def investigate: Boolean = {
    if ( ! With.enemies.exists(_.isProtoss)) return false
    lazy val opponentHasForge = With.units.enemy.exists(_.is(Protoss.Forge))
    With.units.enemy.exists(u =>
      u.is(Protoss.PhotonCannon)
      && (
        u.base.exists(_.owner.isUs)
        || u.zone.edges.exists(_.otherSideof(u.zone).bases.exists(_.owner.isUs))))
  }
}
