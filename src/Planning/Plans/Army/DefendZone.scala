package Planning.Plans.Army

import Information.Geography.Types.Zone
import Micro.Squads.Goals.GoalDefendZone
import Micro.Squads.Squad
import Planning.Plan
import ProxyBwapi.UnitInfo.ForeignUnitInfo

class DefendZone(zone: Zone) extends Plan {
  
  val squad: Squad = new Squad(this)
  val goal: GoalDefendZone = new GoalDefendZone(zone)
  var enemies: Seq[ForeignUnitInfo] = Seq.empty
  
  override def onUpdate() {
  
    if (enemies.size < 3 && enemies.forall(e => e.unitClass.isWorker || ! e.canAttack)) {
      return
    }
  
    squad.setGoal(goal)
    squad.enemies = enemies
    squad.commission()
  }
}
