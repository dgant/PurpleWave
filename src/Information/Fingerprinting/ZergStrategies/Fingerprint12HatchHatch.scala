package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.FingerprintCompleteBy
import Information.Fingerprinting.ZergStrategies.ZergTimings.TwelveHatch11Pool13Hatch_HatchCompleteBy
import Utilities.UnitFilters.IsHatchlike
import Utilities.Time.Seconds

class Fingerprint12HatchHatch extends FingerprintCompleteBy(IsHatchlike, TwelveHatch11Pool13Hatch_HatchCompleteBy - Seconds(3), 3)
