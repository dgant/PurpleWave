package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import bwapi.Race

class FingerprintGatewayFE extends FingerprintAnd(
  new FingerprintRace(Race.Protoss),
  new FingerprintNot(With.intelligence.fingerprints.proxyGateway),
  new FingerprintNot(With.intelligence.fingerprints.cannonRush),
  new FingerprintNot(With.intelligence.fingerprints.twoGate),
  new FingerprintNot(With.intelligence.fingerprints.forgeFe),
  new FingerprintNot(With.intelligence.fingerprints.nexusFirst),
  new FingerprintAnd(
    new FingerprintOr(
      new FingerprintCompleteBy(Protoss.Gateway,  GameTime(3,  0)),
      new FingerprintArrivesBy(Protoss.Zealot,    GameTime(3, 45))),
    new FingerprintOr(
      new FingerprintScoutedEnemyBases(2,             GameTime(5, 0)),
      new FingerprintCompleteBy(Protoss.Forge,        GameTime(5,  0)),
      new FingerprintCompleteBy(Protoss.PhotonCannon, GameTime(5, 30))))) {
  
  override val sticky = true
}
