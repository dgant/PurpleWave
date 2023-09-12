package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteByArrivesBy, FingerprintNot, FingerprintOr}
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.Time.GameTime

class Fingerprint3HatchMuta extends FingerprintAnd(

  new FingerprintNot(
    With.fingerprints.oneHatchMuta,
    With.fingerprints.twoHatchMuta),

  new FingerprintOr(
    new FingerprintCompleteByArrivesBy(Zerg.Spire,    GameTime(7, 35)),
    new FingerprintCompleteByArrivesBy(Zerg.Mutalisk, GameTime(8, 0))))
