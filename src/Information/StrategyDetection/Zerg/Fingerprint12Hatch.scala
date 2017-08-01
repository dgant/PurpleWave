package Information.StrategyDetection.Zerg

import Information.StrategyDetection._

object Fingerprint12Hatch extends FingerprintAnd(
  FingerprintNot(Fingerprint4Pool),
  FingerprintNot(Fingerprint9Pool),
  FingerprintNot(FingerprintOverpool),
  FingerprintNot(Fingerprint12Pool)
)
