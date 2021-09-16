package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.UnitMatchers.{MatchHatchlike, UnitMatcher}
import ProxyBwapi.Races.Zerg
import Utilities.Time.FrameCount

class FingerprintCompleteBy(
   unitMatcher : UnitMatcher,
   gameTime    : FrameCount,
   quantity    : Int = 1) extends Fingerprint {

  private val endFrame = gameTime()
  override def investigate: Boolean = {
    if (quantity == 1) {
      val produced = With.units.enemy.filter(u => u.unitClass.whatBuilds._1 == unitMatcher || u.unitClass.requiredUnits.exists(_._1 == unitMatcher))
      val proof = produced.find(u => {
        val frameProven = if (u.unitClass.isBuilding) With.frame else With.frame - u.unitClass.buildFrames
        frameProven <= endFrame
      })
    }
    if (With.frame > endFrame) return false


    observed >= quantity
  }

  private def observed: Int = {
    var output = With.units.countEverP(u => u.isEnemy && (unitMatcher(u) && u.completionFrame <= endFrame))
    if (unitMatcher == MatchHatchlike) {
      output += With.geography.enemyBases.count(_.townHall.exists(u => u.isEnemy && u.isAny(Zerg.Lair, Zerg.Hive)))
    }
    output
  }

  override def reason: String = f"$observed $unitMatcher complete by $gameTime. Incomplete: [${With.units.enemy.filter(u => unitMatcher(u) && u.completionFrame > endFrame). mkString(", ")}]"

  override def sticky: Boolean = true
}
