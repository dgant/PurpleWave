package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.GameTime

class Fingerprint2RaxAcad extends FingerprintAnd(
  new FingerprintOr(
    With.fingerprints.bbs,
    With.fingerprints.twoRax1113,
    new FingerprintArrivesBy(Terran.Marine, GameTime(6, 30), 11)),
  new FingerprintOr(
    new FingerprintCompleteBy(Terran.Academy,   GameTime(5, 10)),
    new FingerprintTechBy(Terran.Stim,          GameTime(6, 0)),
    new FingerprintArrivesBy(Terran.Firebat,    GameTime(6, 0), 1),
    new FingerprintArrivesBy(Terran.Medic,      GameTime(6, 0), 1)))
