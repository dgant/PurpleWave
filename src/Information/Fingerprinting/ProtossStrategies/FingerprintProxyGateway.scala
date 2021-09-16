package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Fingerprint
import Information.Fingerprinting.Generic._
import Lifecycle.With
import Planning.UnitMatchers.{MatchAnd, MatchProxied}
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class FingerprintProxyGateway extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.nexusFirst),
  new FingerprintOr(
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(2, 50)),
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(3, 15), 2),
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(3, 40), 4),
    new FingerprintCompleteBy(MatchAnd(Protoss.Gateway, MatchProxied), GameTime(5,  0)),
    new Fingerprint {
      override protected def investigate: Boolean = (
        With.frame > GameTime(1, 30)()
        && With.frame < GameTime(4, 0)()
        && With.units.countEnemy(Protoss.Gateway) == 0
        && With.units.countEnemy(Protoss.Forge) == 0
        && With.scouting.enemyMainFullyScouted)
      override protected val reason: String = "Main empty"
    })) {



  // Stick only once we have some affirmative proof, so we don't permanently overreact against Nexus-first (which has an empty main)
  override def sticky: Boolean = With.units.countEnemy(Protoss.Zealot) > 0 || With.units.countEnemy(Protoss.Gateway) > 0
}
