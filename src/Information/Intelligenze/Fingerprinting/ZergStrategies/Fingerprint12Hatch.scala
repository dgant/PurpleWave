package Information.Intelligenze.Fingerprinting.ZergStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintAnd, FingerprintNot, FingerprintRace, FingerprintScoutedEnemyBases}
import Lifecycle.With
import bwapi.Race

class Fingerprint12Hatch extends FingerprintAnd(
  new FingerprintRace(Race.Zerg),
  new FingerprintScoutedEnemyBases(2),
  new FingerprintNot(With.fingerprints.fourPool),
  new FingerprintNot(With.fingerprints.ninePool),
  new FingerprintNot(With.fingerprints.overpool),
  new FingerprintNot(With.fingerprints.tenHatch),
  new FingerprintNot(With.fingerprints.twelvePool)
)
