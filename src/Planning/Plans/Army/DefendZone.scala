package Planning.Plans.Army

import Information.Geography.Types.Zone
import Micro.Squads.Goals.GoalDefendZone
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.ForeignUnitInfo

class DefendZone(zone: Zone) extends SquadPlan[GoalDefendZone] {
  
  val goal: GoalDefendZone = new GoalDefendZone
  var enemies: Seq[ForeignUnitInfo] = Seq.empty
  
  override def onUpdate() {
  
    if (enemies.size < 3 && enemies.forall(e => (e.unitClass.isWorker || ! e.canAttack) && (e.isOverlord() || ! e.unitClass.isTransport))) {
      return
    }

    if (enemies.forall(e => e.is(Protoss.Observer) && ! e.matchups.enemyDetectors.exists(_.canMove))) {
      return
    }
  
    squad.enemies = enemies
    goal.zone = zone
    super.onUpdate()
  }
}
