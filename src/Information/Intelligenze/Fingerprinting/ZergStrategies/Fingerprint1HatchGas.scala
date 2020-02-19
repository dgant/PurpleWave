package Information.Intelligenze.Fingerprinting.ZergStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Zerg

class Fingerprint1HatchGas extends FingerprintOr(
  new FingerprintCompleteBy(Zerg.Extractor, GameTime(2, 20))) { // 10 Hatch 9 Pool 9 Gas finishes gas 2:29

  override val sticky = true
}
