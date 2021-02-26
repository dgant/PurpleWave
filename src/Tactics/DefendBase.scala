package Tactics

import Information.Geography.Types.Base
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.ForeignUnitInfo

class DefendBase(base: Base) {
  var enemies: Seq[ForeignUnitInfo] = Seq.empty
  
  def update() {
  
    if (enemies.size < 3 && enemies.forall(e => (e.unitClass.isWorker || ! e.canAttack) && ! e.isTransport)) {
      return
    }

    if (enemies.forall(e => e.is(Protoss.Observer) && ! e.matchups.enemyDetectors.exists(_.canMove))) {
      return
    }

    With.blackboard.defendingBase.set(true)
  }
}
