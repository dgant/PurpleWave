package Information.Battles.Types

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.Seconds

class Hysteresis(val unit: FriendlyUnitInfo) {
  var lastChangeFrame                = 0
  var lastShouldFight                = true
  private var lastUpdateFrame        = With.frame
  private var bloodlust              = 0.0
  private var bloodlustAtEngage      = 0.0
  private var bloodlustEngagedFrame  = 0
  private var bloodlustDecaying      = false

  def update(): Unit = {
    val shouldFight = unit.agent.shouldFight
    if (shouldFight != lastShouldFight) {
      lastChangeFrame = With.frame
      lastShouldFight = shouldFight
    }
    val engaged = unit.matchups.engaged
    if (engaged && ! bloodlustDecaying) {
      bloodlustAtEngage      = bloodlust
      bloodlustEngagedFrame  = With.frame
      bloodlustDecaying      = true
    }
    if (bloodlustDecaying) {
      bloodlust = bloodlustFrames
      // Finish the decay even if contact is briefly lost; don't restart it on re-entry.
      if (bloodlust == 0 && ! engaged) bloodlustDecaying = false
    } else if (unit.combatJudgment.exists(j => j.scoreTotal > 0 && ! j.unitShouldFight(unit))) {
      bloodlust = Math.min(Seconds(15)(), bloodlust + With.framesSince(lastUpdateFrame))
    }
    lastUpdateFrame = With.frame
  }

  def decisionFrames  : Int    = With.framesSince(lastChangeFrame)
  def bloodlustFrames : Double = if (bloodlustDecaying) {
    bloodlustAtEngage * Math.max(0, 1 - With.framesSince(bloodlustEngagedFrame) / 24.0)
  } else bloodlust
}
