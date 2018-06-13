package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import bwapi.Race

class Fingerprint1GateCore extends FingerprintAnd(
  new FingerprintAnd(
    new FingerprintRace(Race.Protoss),
    new FingerprintScoutedEnemyBases(1),
    new FingerprintNot(With.fingerprints.proxyGateway),
    new FingerprintNot(With.fingerprints.cannonRush),
    new FingerprintNot(With.fingerprints.twoGate),
    new FingerprintNot(With.fingerprints.forgeFe),
    new FingerprintNot(With.fingerprints.gatewayFe),
    new FingerprintNot(With.fingerprints.nexusFirst)),
  new FingerprintOr(
    new FingerprintCompleteBy(Protoss.CyberneticsCore,  GameTime(4, 0)),
    new FingerprintCompleteBy(Protoss.Dragoon,          GameTime(5, 0)))) {
  
  override val sticky = true
}
