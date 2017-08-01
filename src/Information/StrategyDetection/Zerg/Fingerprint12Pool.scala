package Information.StrategyDetection.Zerg

import Information.StrategyDetection._
import ProxyBwapi.Races.Zerg

object Fingerprint12Pool extends FingerprintOr(
  FingerprintNot(Fingerprint4Pool),
  FingerprintNot(Fingerprint9Pool),
  FingerprintNot(FingerprintOverpool),
  FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 40))
)
