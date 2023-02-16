package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.Time.Forever

class FingerprintHasExpanded(by: Int = Forever()) extends Fingerprint {
  override val sticky = true
  override def investigate: Boolean = With.frame < by && With.enemies.exists(e => {
    var output = e.bases.size > 1
    output ||= With.units.enemy.exists(u => Terran.CommandCenter(u) && u.player == e && ! u.base.exists(_.townHallTile == u.tileTopLeft))
    output ||= e.bases.exists( ! _.isStartLocation)
    output
  })
}
