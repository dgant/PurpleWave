package Planning.Plans.Army

import Information.Geography.Types.Zone
import Micro.Squads.Goals.GoalDefendZone
import ProxyBwapi.UnitInfo.ForeignUnitInfo

class DefendZone(zone: Zone) extends SquadPlan[GoalDefendZone] {
  
  val goal: GoalDefendZone = new GoalDefendZone
  var enemies: Seq[ForeignUnitInfo] = Seq.empty
  
  override def onUpdate() {
  
    if (enemies.size < 3 && enemies.forall(e => e.unitClass.isWorker || ! e.canAttack)) {
      return
    }
    
    goal.zone = zone
    goal.squad.enemies = enemies
    super.onUpdate()
  }
}
