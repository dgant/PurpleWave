package Information.StrategyDetection.ZergStrategies

import Information.StrategyDetection.Generic.{FingerprintAnd, FingerprintNot, FingerprintRace, FingerprintScoutedEnemyBase}
import bwapi.Race

object Fingerprint12Hatch extends FingerprintAnd(
  FingerprintRace(Race.Zerg),
  FingerprintScoutedEnemyBase,
  FingerprintNot(Fingerprint4Pool),
  FingerprintNot(Fingerprint9Pool),
  FingerprintNot(FingerprintOverpool),
  FingerprintNot(Fingerprint10Hatch9Pool),
  FingerprintNot(Fingerprint12Pool)
)
