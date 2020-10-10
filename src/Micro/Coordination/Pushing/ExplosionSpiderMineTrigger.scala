package Micro.Coordination.Pushing

import Mathematics.Physics.Force
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

// Spider Mines rely on normal target acquisition AI range math, which is 96px + attack range edge-to-edge
// However, this is often limited by Spider Mine sight range, which is 96px exactly.
// According to jaj22 the acqusition range is center-to-center (unlike most units, which are edge-to-edge)
object SpiderMineActivation {
  val radius = 128
}

class ExplosionSpiderMineTrigger(mine: UnitInfo) extends CircularPush(PushPriority.Dodge, mine.pixelCenter, SpiderMineActivation.radius) {
  override def force(recipient: FriendlyUnitInfo): Option[Force] = {
    if (recipient.flying) {
      None
    } else if (
      mine.detected
      && recipient.canAttack(mine)
      && recipient.pixelRangeAgainst(mine) > SpiderMineActivation.radius - (if(recipient.visibleToOpponents) 0 else 32)) {
      None
    } else {
      super.force(recipient)
    }
  }
}
