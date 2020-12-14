package Planning.Plans.Army

import Information.Geography.Types.Base
import Lifecycle.With
import Micro.Squads.Goals.GoalDefendZone
import Planning.UnitMatchers.UnitMatchRecruitableForCombat
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.ForeignUnitInfo

class DefendBase(base: Base) extends SquadPlan[GoalDefendZone] {
  
  val goal: GoalDefendZone = new GoalDefendZone
  goal.unitMatcher = UnitMatchRecruitableForCombat

  var enemies: Seq[ForeignUnitInfo] = Seq.empty
  
  override def onUpdate() {
    With.blackboard.defendingBase.set(true)
  
    if (enemies.size < 3 && enemies.forall(e => (e.unitClass.isWorker || ! e.canAttack) && (e.isOverlord() || ! e.unitClass.isTransport))) {
      return
    }

    if (enemies.forall(e => e.is(Protoss.Observer) && ! e.matchups.enemyDetectors.exists(_.canMove))) {
      return
    }
  
    squad.enemies = enemies
    goal.zone = base.zone
    super.onUpdate()
  }
}
