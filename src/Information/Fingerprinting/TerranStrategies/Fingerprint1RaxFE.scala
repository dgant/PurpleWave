package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintOr}
import ProxyBwapi.Races.Terran
import Utilities.Time.GameTime

class Fingerprint1RaxFE extends FingerprintAnd(
  new FingerprintOr(
    new FingerprintCompleteBy(Terran.Barracks, GameTime(3, 20)),
    new FingerprintCompleteBy(Terran.Marine, GameTime(3, 35)),
    new FingerprintCompleteBy(Terran.Bunker, GameTime(3, 39))),
  new FingerprintCompleteBy(Terran.CommandCenter,  GameTime(4, 30), 2)) {
  override val sticky = true
}
