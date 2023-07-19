package Information.Fingerprinting.ZergStrategies

import Utilities.Time.{GameTime, Seconds}

/**
  * Records the fastest-observed completion of specific buildings in Zerg build orders.
  * Used to construct Zerg build fingerprints.
  * Many of these are likely unused but it's handy to have them on record.
  */
object ZergTimings {
  object OneHatchGasCompleteBy                    extends GameTime(2, 20)
  object TwoHatchGasCompleteBy                    extends GameTime(2, 36)
  object ThreeHatchGasCompleteBy                  extends GameTime(3, 10)

  object FivePool_PoolCompleteBy                  extends GameTime(1, 32)
  object NinePool_PoolCompleteBy                  extends GameTime(1, 58)
  object NinePool9Gas_GasCompleteBy               extends GameTime(1, 43)
  object NinePool9Hatch_HatchCompleteBy           extends GameTime(3, 7) // Hatch before Overlord. I think it's an awful build but bots have done it
  object NinePool10Hatch_HatchCompleteBy          extends GameTime(3, 18)
  object NinePool13Hatch_HatchCompleteBy          extends GameTime(3, 35)
  object NinePool13Hatch12Gas_GasCompleteBy       extends GameTime(2, 57)
  object Overpool_PoolCompleteBy                  extends GameTime(2, 7)
  object Overpool8Gas_GasCompleteBy               extends GameTime(1, 53)
  object Overpool9Gas_GasCompleteBy               extends GameTime(2, 0) // Then either Lair/Speed starts on 100 gas
  object Overpool11Hatch_HatchCompleteBy          extends GameTime(3, 19)
  object TwelvePool_PoolCompleteBy                extends GameTime(2, 22)
  object TwelvePool11Gas_GasCompleteBy            extends GameTime(2, 3)
  object TwelvePool12Gas_GasCompleteBy            extends GameTime(2, 11)
  object TwelvePool11Gas10Hatch_HatchCompleteBy   extends GameTime(3, 28)
  object TwelvePool11Gas10Hatch_SpeedCompleteBy   extends GameTime(3, 42) // Could probably be done a bit sooner with some build tweaks
  object TwelvePool12Gas11Hatch_HatchCompleteBy   extends GameTime(3, 23)
  object TwelvePool12Gas10Hatch_SpeedCompleteBy   extends GameTime(3, 46) // Could probably be done a bit sooner with some build tweaks
  object TenHatch_HatchCompleteBy                 extends GameTime(2, 40)
  object TenHatch9Pool_PoolCompleteBy             extends GameTime(2, 37)
  object TenHatch9Pool8Gas_GasCompleteBy          extends GameTime(2, 23) // Speculative. Didn't have a replay to measure. Estimated based on 14 seconds of 12h11p10 and overpool8gas
  object TwelveHatch_HatchCompleteBy              extends GameTime(3, 0)
  object TwelveHatch11Pool_PoolCompleteBy         extends GameTime(2, 51)
  object TwelveHatch11Pool10Gas_GasCompleteBy     extends GameTime(2, 38)
  object TwelveHatch11Pool13Hatch_HatchCompleteBy extends GameTime(3, 50)
  object TwelveHatch12Pool_PoolCompleteBy         extends GameTime(2, 56)
  object TwelveHatch12Pool_GasCompleteBy          extends GameTime(2, 43)
  object TwelveHatch13Hatch_3ndHatchCompleteBy    extends GameTime(3, 46)
  object TwelveHatch13Hatch12Pool_PoolCompleteBy  extends GameTime(3, 43)

  object Mine100Gas extends Seconds(24)
}
