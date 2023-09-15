package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr}
import Information.Fingerprinting.ZergStrategies.ZergTimings.{Latest_FourPool_PoolCompleteBy, Latest_FourPool_ZerglingArrivesBy, Latest_FourPool_ZerglingCompleteBy}
import ProxyBwapi.Races.Zerg

class Fingerprint4Pool extends FingerprintOr(
  new FingerprintCompleteBy(Zerg.SpawningPool,  Latest_FourPool_PoolCompleteBy),
  new FingerprintArrivesBy(Zerg.Zergling,       Latest_FourPool_ZerglingArrivesBy),
  new FingerprintCompleteBy(Zerg.Zergling,      Latest_FourPool_ZerglingCompleteBy))