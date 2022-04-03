package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.FingerprintCompleteBy
import Information.Fingerprinting.Strategies.ZergTimings
import Utilities.UnitMatchers.MatchHatchlike
import Utilities.Time.Seconds

class Fingerprint12HatchHatch extends FingerprintCompleteBy(MatchHatchlike, ZergTimings.TwelveHatch11Pool13Hatch_HatchCompleteBy - Seconds(3), 3)
