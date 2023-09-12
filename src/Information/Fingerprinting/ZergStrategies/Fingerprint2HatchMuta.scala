package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintCompleteByArrivesBy, FingerprintNot, FingerprintOr}
import Information.Fingerprinting.ZergStrategies.ZergTimings.ThreeHatchMuta_MutaCompleteBy
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.Time.{Frames, GameTime, Seconds}

class Fingerprint2HatchMuta extends FingerprintAnd(

  new FingerprintNot(
    With.fingerprints.oneHatchMuta,
    With.fingerprints.threeHatchGas,
    With.fingerprints.twelveHatchHatch),

  new FingerprintOr(

    new FingerprintCompleteBy(Zerg.Spire,             ThreeHatchMuta_MutaCompleteBy - Seconds(5) - Frames(Zerg.Spire.buildFrames))),
    new FingerprintCompleteByArrivesBy(Zerg.Mutalisk, ThreeHatchMuta_MutaCompleteBy - Seconds(5)),

    new FingerprintAnd(
      With.fingerprints.twoHatchGas,
      new FingerprintOr(
        new FingerprintCompleteByArrivesBy(Zerg.Spire,    GameTime(7, 35)),
        new FingerprintCompleteByArrivesBy(Zerg.Mutalisk, GameTime(8, 0)))))