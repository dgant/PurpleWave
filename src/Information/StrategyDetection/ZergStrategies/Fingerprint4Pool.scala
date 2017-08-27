package Information.StrategyDetection.ZergStrategies

import Information.StrategyDetection.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Zerg

class Fingerprint4Pool extends FingerprintOr(
  new FingerprintArrivesBy(Zerg.Zergling,       GameTime(2, 50)),
  new FingerprintCompleteBy(Zerg.SpawningPool,  GameTime(1, 40))) {
  
  
  var matchedBefore = false
  override def matches: Boolean = {
    if (super.matches && ! matchedBefore) {
      val setBreakpointHere = 12345
    }
    matchedBefore = super.matches
    matchedBefore
  }
}
