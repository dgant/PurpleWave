package Information.Intelligenze.Fingerprinting.ZergStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Zerg

class Fingerprint1HatchGas extends FingerprintOr(
  new FingerprintCompleteBy(Zerg.Extractor, GameTime(2, 30))) {

  override val sticky = true
}
