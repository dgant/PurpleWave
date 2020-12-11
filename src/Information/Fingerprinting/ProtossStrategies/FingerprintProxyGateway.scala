package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Fingerprint
import Information.Fingerprinting.Generic._
import Lifecycle.With
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchProxied}
import ProxyBwapi.Races.Protoss
import Utilities.GameTime

class FingerprintProxyGateway extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.nexusFirst),
  new FingerprintOr(
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(2, 50)),
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(3, 15), 2),
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(3, 50), 4),
    new FingerprintCompleteBy(UnitMatchAnd(Protoss.Gateway, UnitMatchProxied), GameTime(5,  0)),
    new Fingerprint {
      override protected def investigate: Boolean = (
        With.frame > GameTime(1, 30)()
        && With.frame < GameTime(4, 0)()
        && With.units.countEnemy(Protoss.Gateway) == 0
        && With.units.countEnemy(Protoss.Forge) == 0
        && With.geography.enemyBases.exists(_.owner.isProtoss)
        && With.geography.enemyBases.filter(_.isStartLocation).forall(base => {
          val scoutableTiles = base.zone.tiles.view.filter(With.grids.buildableTerrain.get)
          val tilesSeen = scoutableTiles.count(tile => tile.valid && With.grids.friendlyVision.rawValues(tile.i) > 0)
          tilesSeen >= scoutableTiles.size * 0.9
        }))
    })) {

  // Stick only once we have some affirmative proof, so we don't permanently overreact against Nexus-first (which has an empty main)
  override def sticky = With.units.countEnemy(Protoss.Zealot) > 0 || With.units.countEnemy(Protoss.Gateway) > 0
}
