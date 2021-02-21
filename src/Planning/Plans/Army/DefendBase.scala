package Planning.Plans.Army

import Information.Geography.Types.Base
import Lifecycle.With
import Micro.Squads.Goals.GoalDefendZone
import Planning.UnitMatchers.MatchRecruitableForCombat
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.ForeignUnitInfo

class DefendBase(base: Base) extends Squadify[GoalDefendZone] {
  
  val goal: GoalDefendZone = new GoalDefendZone
  goal.unitMatcher = MatchRecruitableForCombat

  var enemies: Seq[ForeignUnitInfo] = Seq.empty
  
  override def update() {
  
    if (enemies.size < 3 && enemies.forall(e => (e.unitClass.isWorker || ! e.canAttack) && ! e.isTransport)) {
      return
    }

    if (enemies.forall(e => e.is(Protoss.Observer) && ! e.matchups.enemyDetectors.exists(_.canMove))) {
      return
    }

    With.blackboard.defendingBase.set(true)
    goal.zone = base.zone
    super.update()
  }
}
