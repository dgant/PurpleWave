package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import Utilities.Time.GameTime
import bwapi.Race

class Fingerprint12Hatch extends FingerprintAnd(
  new FingerprintRace(Race.Zerg),
  new FingerprintNot(With.fingerprints.fourPool),
  new FingerprintNot(With.fingerprints.ninePool),
  new FingerprintNot(With.fingerprints.overpool),
  new FingerprintNot(With.fingerprints.tenHatch),
  new FingerprintNot(With.fingerprints.twelvePool),
  new FingerprintCompleteBy(
    unit=> unit.unitClass.isTownHall && ! unit.base.exists(b => b.isStartLocation && b.townHallTile == unit.tileTopLeft),
    GameTime(3, 10)))