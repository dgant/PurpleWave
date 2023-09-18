package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Mathematics.Maff
import Performance.Cache
import ProxyBwapi.Races.Zerg
import Utilities.?
import Utilities.Time.Minutes

class FingerprintSpeedBeforeLair extends Fingerprint {
  var firstExtractorFrame : Option[Int] = None
  var firstLairFrame      : Option[Int] = None
  var firstSpeedFrame     : Option[Int] = None
  var speedStartFrame     : Option[Int] = None
  var gasAtFirstLair      : Option[Int] = None

  override protected def investigate: Boolean = {
    firstExtractorFrame = Maff.min(With.units.enemy.filter(u => u.unitClass.isGas).map(_.completionFrame))
    firstLairFrame      = firstLairFrame  .orElse(Maff.min(With.units.enemy.filter(Zerg.Lair).map(_.lastClassChange)))
    firstSpeedFrame     = firstSpeedFrame .orElse(?(speedDone, Some(With.frame), None))
    speedStartFrame     = firstSpeedFrame .map(_ - Zerg.ZerglingSpeed.upgradeFrames(1))
    gasAtFirstLair      = gasAtFirstLair  .orElse(?(firstLairFrame.isDefined, Some(gasMined()), None))

    if (speedStartFrame.isDefined && firstLairFrame.isDefined) {
      return speedStartFrame.get < firstLairFrame.get
    }

    if (gasAtFirstLair.isDefined) {
      return gasAtFirstLair.get < 200
    }

    gasMined() > 110
  }

  private val gasMined = new Cache(() => {
    (With.accounting.workerIncomePerFrameGas *
      Math.min(
        // Maximum possible gas mining time
        firstExtractorFrame.map(With.framesSince(_) * 3).getOrElse(0),
        // Gas mining time observed from workers
        With.units.enemy
          .map(e => With.framesSince(e.firstFrameMiningGas))
          .filter(_ > 0)
          .sum)).toInt
  })

  private def speedDone = With.enemies.exists(_.hasUpgrade(Zerg.ZerglingSpeed))

  override def sticky: Boolean = With.frame > Minutes(6)() || speedDone
}
