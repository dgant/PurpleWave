package Information.Intelligence.Fingerprinting.ZergStrategies

import Information.Intelligence.Fingerprinting.Generic.{FingerprintAnd, FingerprintNot, FingerprintRace, FingerprintScoutedEnemyBases}
import Lifecycle.With
import bwapi.Race

class Fingerprint12Hatch extends FingerprintAnd(
  new FingerprintRace(Race.Zerg),
  new FingerprintScoutedEnemyBases(2),
  new FingerprintNot(With.intelligence.fingerprints.fingerprint4Pool),
  new FingerprintNot(With.intelligence.fingerprints.fingerprint9Pool),
  new FingerprintNot(With.intelligence.fingerprints.fingerprintOverpool),
  new FingerprintNot(With.intelligence.fingerprints.fingerprint10Hatch9Pool),
  new FingerprintNot(With.intelligence.fingerprints.fingerprint12Pool)
)
