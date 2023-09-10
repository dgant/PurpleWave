package Information.Fingerprinting.ZergStrategies

import ProxyBwapi.Races.Zerg
import Utilities.Time.{FrameCount, GameTime, Seconds}

/**
  * Records the fastest-observed completion of specific buildings in Zerg build orders.
  * Used to construct Zerg build fingerprints.
  * Many of these are likely unused but it's handy to have them on record.
  */
object ZergTimings {
  object OneHatchGasCompleteBy                    extends GameTime(2, 20)
  object TwoHatchGasCompleteBy                    extends GameTime(2, 36)
  object ThreeHatchGasCompleteBy                  extends GameTime(3, 10)

  object FourPool_PoolCompleteBy                  extends GameTime(1, 32)
  object SevenPool_PoolCompleteBy                 extends GameTime(1, 42) // Estimated from one Crona replay
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

  object OneHatchMuta_MutaCompleteBy              extends GameTime(4, 55) // SCWes.
  object TwelvePoolhMuta_MutaCompleteBy           extends GameTime(5, 23) // SCWes.
  object TwoHatchMuta_MutaCompleteBy              extends GameTime(5, 47) // SCWes. Also: 12h11g10p -> Muta @ 5:47
  object TwoPointFiveHatchMuta_MutaCompleteBy     extends GameTime(5, 58) // Effort: https://youtu.be/srXJUxekyLk?t=374
  object ThreeHatchMuta_MutaCompleteBy            extends GameTime(6, 28) // Based on Liquipedia gives 4:48 - 4:52 Spire start; SCWes gives 4:52 Lair finish. https://youtu.be/FFOpaQD6Rtk corroborates
  object Mine100Gas extends Seconds(24)


  // These are designed to be conservative bounds,
  // eg. inclusive of slower execution but not overlapping the best execution of the next-fastest pool timing
  //
  // 9 Pool and Overpool are particularly hard to differentiate because there's only a ~9 second difference
  // to acquire the extra 100 minerals that Overpool requires for its Spawning Pool.

  private val PresumedZerglingRushTime = Seconds(30)

  val Latest_FourPool_PoolCompleteBy        : FrameCount = SevenPool_PoolCompleteBy             - Seconds(3)
  val Latest_NinePool_PoolCompleteBy        : FrameCount = Overpool_PoolCompleteBy              - Seconds(5)
  val Latest_Overpool_PoolCompleteBy        : FrameCount = TwelvePool_PoolCompleteBy            - Seconds(3)
  val Latest_TwelvePool_PoolCompleteBy      : FrameCount = TenHatch9Pool_PoolCompleteBy         - Seconds(7) // Narrower tolerance because 9H9P, which we categorize as 10H9P, would get pool a little earlier but we don't track it
  val Latest_TenHatch_PoolCompleteBy        : FrameCount = TwelveHatch11Pool_PoolCompleteBy     - Seconds(11) // Picked just to make previously-tuned timings the same. Can probably be reduced.

  val Latest_FourPool_ZerglingCompleteBy    : FrameCount = Latest_FourPool_PoolCompleteBy       + Zerg.Zergling.buildFrames
  val Latest_NinePool_ZerglingCompleteBy    : FrameCount = Latest_NinePool_PoolCompleteBy       + Zerg.Zergling.buildFrames
  val Latest_Overpool_ZerglingCompleteBy    : FrameCount = Latest_Overpool_PoolCompleteBy       + Zerg.Zergling.buildFrames
  val Latest_TwelvePool_ZerglingCompleteBy  : FrameCount = Latest_TwelvePool_PoolCompleteBy     + Zerg.Zergling.buildFrames
  val Latest_TenHatch_ZerglingCompleteBy    : FrameCount = Latest_TenHatch_PoolCompleteBy       + Zerg.Zergling.buildFrames

  val Latest_FourPool_ZerglingArrivesBy     : FrameCount = Latest_FourPool_ZerglingCompleteBy   + PresumedZerglingRushTime
  val Latest_NinePool_ZerglingArrivesBy     : FrameCount = Latest_NinePool_ZerglingCompleteBy   + PresumedZerglingRushTime
  val Latest_Overpool_ZerglingArrivesBy     : FrameCount = Latest_Overpool_ZerglingCompleteBy   + PresumedZerglingRushTime
  val Latest_TwelvePool_ZerglingArrivesBy   : FrameCount = Latest_TwelvePool_ZerglingCompleteBy + PresumedZerglingRushTime
  val Latest_TenHatch_ZerglingArrivesBy     : FrameCount = Latest_TenHatch_ZerglingCompleteBy   + PresumedZerglingRushTime

}
