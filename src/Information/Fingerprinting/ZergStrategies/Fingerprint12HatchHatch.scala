package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.FingerprintCompleteBy
import Information.Fingerprinting.Strategies.ZergTimings
import Utilities.UnitFilters.IsHatchlike
import Utilities.Time.Seconds

class Fingerprint12HatchHatch extends FingerprintCompleteBy(IsHatchlike, ZergTimings.TwelveHatch11Pool13Hatch_HatchCompleteBy - Seconds(3), 3)
