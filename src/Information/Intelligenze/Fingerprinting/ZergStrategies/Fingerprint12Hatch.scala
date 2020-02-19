package Information.Intelligenze.Fingerprinting.ZergStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import Planning.UnitMatchers.UnitMatchNonStartingTownHall
import bwapi.Race

class Fingerprint12Hatch extends FingerprintAnd(
  new FingerprintRace(Race.Zerg),
  new FingerprintProvenBases(2),
  new FingerprintNot(With.fingerprints.fourPool),
  new FingerprintNot(With.fingerprints.ninePool),
  new FingerprintNot(With.fingerprints.overpool),
  new FingerprintNot(With.fingerprints.tenHatch),
  new FingerprintNot(With.fingerprints.twelvePool),
  new FingerprintCompleteBy(UnitMatchNonStartingTownHall, GameTime(3, 10)))