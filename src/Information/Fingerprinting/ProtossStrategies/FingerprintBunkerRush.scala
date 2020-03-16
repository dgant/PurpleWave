package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchProxied}
import ProxyBwapi.Races.Terran

class FingerprintBunkerRush extends FingerprintCompleteBy(
  UnitMatchAnd(Terran.Bunker, UnitMatchProxied),
  GameTime(5,  0)) {
  
  override val sticky = true
}