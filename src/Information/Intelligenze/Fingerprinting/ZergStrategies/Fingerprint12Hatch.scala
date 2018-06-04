package Information.Intelligenze.Fingerprinting.ZergStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintAnd, FingerprintNot, FingerprintRace, FingerprintScoutedEnemyBases}
import Lifecycle.With
import bwapi.Race

class Fingerprint12Hatch extends FingerprintAnd(
  new FingerprintRace(Race.Zerg),
  new FingerprintScoutedEnemyBases(2),
  new FingerprintNot(With.intelligence.fingerprints.fourPool),
  new FingerprintNot(With.intelligence.fingerprints.ninePool),
  new FingerprintNot(With.intelligence.fingerprints.overpool),
  new FingerprintNot(With.intelligence.fingerprints.tenHatchNinePool),
  new FingerprintNot(With.intelligence.fingerprints.twelvePool)
)
