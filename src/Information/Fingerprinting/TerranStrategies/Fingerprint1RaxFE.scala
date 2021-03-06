package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy}
import ProxyBwapi.Races.Terran
import Utilities.GameTime

class Fingerprint1RaxFE extends FingerprintAnd(
  new FingerprintCompleteBy(Terran.Barracks, GameTime(3, 0)),
  new FingerprintCompleteBy(Terran.CommandCenter,  GameTime(4, 30), 2)) {
  override val sticky = true
}
