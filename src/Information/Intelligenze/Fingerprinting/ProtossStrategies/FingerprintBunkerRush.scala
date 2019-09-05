package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchProxied}
import ProxyBwapi.Races.Terran

class FingerprintBunkerRush extends FingerprintCompleteBy(
  UnitMatchAnd(Terran.Bunker, UnitMatchProxied),
  GameTime(4,  30)) {
  
  override val sticky = true
}