package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import bwapi.Race

class FingerprintForgeFE extends FingerprintAnd(
  new FingerprintRace(Race.Protoss),
  new FingerprintNot(With.intelligence.fingerprints.proxyGateway),
  new FingerprintNot(With.intelligence.fingerprints.cannonRush),
  new FingerprintNot(With.intelligence.fingerprints.twoGate),
  new FingerprintNot(With.intelligence.fingerprints.nexusFirst),
  new FingerprintOr(
    new FingerprintCompleteBy(Protoss.Forge,        GameTime(3,  0)),
    new FingerprintCompleteBy(Protoss.PhotonCannon, GameTime(3,  30)))) {
  
  override val sticky = true
}
