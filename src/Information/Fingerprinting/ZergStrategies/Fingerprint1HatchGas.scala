package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintCompleteBy, FingerprintOr}
import ProxyBwapi.Races.Zerg
import Utilities.GameTime

class Fingerprint1HatchGas extends FingerprintOr(
  new FingerprintCompleteBy(Zerg.Extractor, GameTime(2, 20))) { // 10 Hatch 9 Pool 9 Gas finishes gas 2:29

  override val sticky = true
}
