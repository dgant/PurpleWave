package Information.Fingerprinting.Generic

import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.?
import Utilities.Time.{FrameCount, Frames}
import Utilities.UnitFilters.UnitFilter

import scala.util.Try

class FingerprintCompleteByArrivesBy(
  val unitFilter  : UnitFilter,
  val gameTime    : FrameCount,
  val rushTime    : FrameCount,
  val quantity    : Int = 1) extends FingerprintOr(
    new FingerprintCompleteBy(unitFilter, gameTime, quantity),
    new FingerprintArrivesBy(unitFilter, gameTime + rushTime, quantity)) {

  def this(
    unitFilter  : UnitFilter,
    gameTime    : FrameCount,
    quantity    : Int) {

    this(
      unitFilter,
      gameTime,
      {
        val unitClass = Try(unitFilter.asInstanceOf[UnitClass])
          .toOption
          .getOrElse(Terran.Marine)
        Frames(
          ?(unitClass.isFlyer,
            96 * 32 / unitClass.topSpeed,
            190 * 32 / unitClass.topSpeed).toInt)
      },
      quantity)
  }

  def this(unitFilter: UnitFilter, gameTime: FrameCount) {
    this(unitFilter, gameTime, 1)
  }
}
