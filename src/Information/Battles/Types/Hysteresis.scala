package Information.Battles.Types

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

class Hysteresis(val unit: FriendlyUnitInfo) {
  var lastChangeFrame           = 0
  var lastShouldFight           = true
  var lastBloodlustChangeFrame  = 0
  var bloodlust                 = true

  def update(): Unit = {
    val shouldFight = unit.agent.shouldFight
    if (shouldFight != lastShouldFight) {
      lastChangeFrame = With.frame
    }
    val shouldBloodlust = unit.battle.exists(_.judgement.exists(j => j.scoreTotal > 0 && ! j.unitShouldFight(unit)))
    if (shouldBloodlust != bloodlust) {
      bloodlust = shouldBloodlust
      lastBloodlustChangeFrame = With.frame
    }
  }

  def decisionFrames  : Int = With.framesSince(lastChangeFrame)
  def bloodlustFrames : Int = ?(bloodlust, With.framesSince(lastBloodlustChangeFrame), 0)
}
