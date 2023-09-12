package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintCompleteByArrivesBy, FingerprintNot, FingerprintOr}
import Information.Fingerprinting.ZergStrategies.ZergTimings.TwoHatchMuta_MutaCompleteBy
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.Time.{Frames, GameTime, Seconds}

class Fingerprint1HatchMuta extends FingerprintAnd(

  new FingerprintNot(
    With.fingerprints.hatchFirst,
    With.fingerprints.ninePoolHatch,
    With.fingerprints.overpoolHatch,
    With.fingerprints.twoHatchGas,
    With.fingerprints.threeHatchGas,
  ),

  new FingerprintOr(

    new FingerprintCompleteBy(Zerg.Spire,             TwoHatchMuta_MutaCompleteBy - Seconds(5) - Frames(Zerg.Spire.buildFrames)),
    new FingerprintCompleteByArrivesBy(Zerg.Mutalisk, TwoHatchMuta_MutaCompleteBy - Seconds(5)),

    new FingerprintAnd(
      With.fingerprints.oneHatchGas,
      new FingerprintOr(
        new FingerprintCompleteByArrivesBy(Zerg.Spire,    GameTime(7, 35)),
        new FingerprintCompleteByArrivesBy(Zerg.Mutalisk, GameTime(8, 0))))) )// Placeholder time; not vetted
