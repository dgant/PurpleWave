package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Planning.UnitMatchers.{MatchAnd, MatchProxied}
import ProxyBwapi.Races.Terran
import Utilities.GameTime

class FingerprintBunkerRush extends FingerprintCompleteBy(
  MatchAnd(Terran.Bunker, MatchProxied),
  GameTime(5,  0)) {
  
  override val sticky = true
}