package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr}
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.Time.GameTime

class Fingerprint3HatchHydra extends FingerprintAnd(
  With.fingerprints.threeHatchGas,
  new FingerprintOr(
    new FingerprintCompleteBy(Zerg.HydraliskDen, GameTime(5, 0)), // Earliest observed: 4:37
    new FingerprintCompleteBy(Zerg.Hydralisk, GameTime(5, 18)),
    new FingerprintArrivesBy(Zerg.Hydralisk, GameTime(5, 48))))