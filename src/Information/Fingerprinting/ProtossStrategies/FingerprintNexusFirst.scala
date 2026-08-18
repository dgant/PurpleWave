package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class FingerprintNexusFirst extends FingerprintOr(
  new FingerprintCompleteBy(
    u =>
    Protoss.Nexus(u)
    && (
      ! u.base.exists(_.isMain)
      || ! u.complete
      || With.scouting.enemyMain.exists( ! u.base.contains(_))),
    GameTime(3, 30)),
  new FingerprintAnd(
    new FingerprintExistsBy(
      u =>
        Protoss.Pylon(u)
        && With.scouting.enemyMain.exists(main => u.metro.contains(main.metro))
        && With.scouting.enemyMainFullyScouted,
      GameTime(2, 0)),
    new FingerprintNot(
      new FingerprintExistsBy(u =>
        u.isAny(Protoss.Gateway, Protoss.Forge),
        GameTime(2, 0))),
    new FingerprintNot(
      With.fingerprints.gatewayFirst,
      With.fingerprints.earlyForge)))
  {
  override def sticky: Boolean =
    With.frame > GameTime(3, 20)() &&
    ! With.fingerprints.gatewayFirst() &&
    ! With.fingerprints.earlyForge()
}
