package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Fingerprint
import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintHasExpanded, FingerprintNot, FingerprintOr}
import Lifecycle.With
import ProxyBwapi.Races.Terran

class FingerprintSiegeExpand extends FingerprintAnd(
  new FingerprintNot(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE,
    With.fingerprints.bbs,
    With.fingerprints.twoRax1113,
    With.fingerprints.twoFac,
    With.fingerprints.threeFac),
  new FingerprintAnd(
    new FingerprintHasExpanded,
    With.fingerprints.oneFac,
    new FingerprintOr(
      With.fingerprints.wallIn,
      new Fingerprint {
        override protected def reason: String = "Enemy got siege first"
        override protected def investigate: Boolean = {
          With.enemies.exists(e =>
            (Terran.SiegeMode(e) || With.unitsShown(e, Terran.Bunker) > 0)
            && ! Terran.SpiderMinePlant(e)
            && ! Terran.VultureSpeed(e)
            && ! Terran.Stim(e))
        }
        override def sticky: Boolean = With.enemies.filter(_.isTerran).exists(e =>
          Terran.SiegeMode(e)
          || Terran.SpiderMinePlant(e)
          || Terran.VultureSpeed(e)
          || Terran.Stim(e)
        )
      }))) {
  override val sticky: Boolean = true
}
