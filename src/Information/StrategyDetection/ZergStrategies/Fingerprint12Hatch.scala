package Information.StrategyDetection.ZergStrategies

import Information.StrategyDetection.Generic.{FingerprintAnd, FingerprintNot, FingerprintRace, FingerprintScoutedEnemyBase}
import bwapi.Race

class Fingerprint12Hatch extends FingerprintAnd(
  new FingerprintRace(Race.Zerg),
  new FingerprintScoutedEnemyBase,
  new FingerprintNot(new Fingerprint4Pool),
  new FingerprintNot(new Fingerprint9Pool),
  new FingerprintNot(new FingerprintOverpool),
  new FingerprintNot(new Fingerprint10Hatch9Pool),
  new FingerprintNot(new Fingerprint12Pool)
)
