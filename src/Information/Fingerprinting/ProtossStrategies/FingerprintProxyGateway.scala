package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Fingerprint
import Information.Fingerprinting.Generic._
import Lifecycle.With
import Mathematics.Maff
import Utilities.UnitFilters.{IsAll, IsProxied}
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class FingerprintProxyGateway extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.nexusFirst),
  new FingerprintOr(
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(2, 50)),
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(3, 15), 2),
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(3, 40), 4),
    new FingerprintCompleteBy(IsAll(Protoss.Gateway, IsProxied), GameTime(5,  0)),
    new Fingerprint {
      override protected def investigate: Boolean = (
        Maff.betweenI(With.frame, GameTime(1, 30)(), GameTime(4, 0)())
        &&  ! With.units.existsEnemy(Protoss.Gateway)
        &&  ! With.units.existsEnemy(Protoss.Forge)
        &&  With.scouting.enemyMainFullyScouted)
      override protected val reason: String = "Main empty"
    })) {

  // Stick only once we have some affirmative proof, so we don't permanently overreact against Nexus-first (which has an empty main)
  override def sticky: Boolean = With.units.countEnemy(Protoss.Zealot) > 0 || With.units.countEnemy(Protoss.Gateway) > 0
}
