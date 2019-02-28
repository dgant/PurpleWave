package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Fingerprint
import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchProxied}
import ProxyBwapi.Races.Protoss

class FingerprintProxyGateway extends FingerprintAnd(
  new FingerprintOr(
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(2, 50)),
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(3, 15), 2),
    new FingerprintArrivesBy(Protoss.Zealot, GameTime(3, 50), 4),
    new FingerprintCompleteBy(
      UnitMatchAnd(Protoss.Gateway, UnitMatchProxied),
      GameTime(5,  0)),
    new Fingerprint {
      override protected def investigate: Boolean = (
        With.frame > GameTime(1, 30)()
        && With.frame < GameTime(4, 0)()
        && With.geography.enemyBases.exists(base => {
          val scoutableTiles = base.zone.tiles.view.filter(With.grids.buildableTerrain.get)
          (base.isStartLocation
            && scoutableTiles.count(tile => tile.valid && With.grids.friendlyVision.rawValues(tile.i) > 0) >= scoutableTiles.size * 0.9
            && With.units.countEnemy(Protoss.Gateway) == 0
            && With.units.countEnemy(Protoss.Forge) == 0)
        }))
    })) {
  
  override val sticky = true
}
