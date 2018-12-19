package Information.Intelligenze.Fingerprinting.TerranStrategies

import Information.Intelligenze.Fingerprinting.Fingerprint
import Information.Intelligenze.Fingerprinting.Generic.{FingerprintAnd, GameTime}
import Lifecycle.With
import ProxyBwapi.Races.Terran

class FingerprintSiegeExpand extends FingerprintAnd(
  new Fingerprint {
    override protected def investigate: Boolean = {
      With.enemies.exists(e => {
        val siege =
          (With.intelligence.unitsShown(e, Terran.SiegeTankUnsieged)
          + With.intelligence.unitsShown(e, Terran.SiegeTankSieged)) > 0
        val expand = (
          With.intelligence.unitsShown(e, Terran.CommandCenter) > 1
          || With.units.enemy.exists(u => u.player == e && u.isCommandCenter() && ! With.geography.startLocations.contains(u.tileTopLeft))
          || e.bases.size > 1)
        siege && expand
      })
    }
    override val sticky: Boolean = true
    override protected def lockAfter: Int = GameTime(7, 0)()
  })
