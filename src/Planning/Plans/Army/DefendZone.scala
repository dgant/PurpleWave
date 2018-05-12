package Planning.Plans.Army

import Information.Geography.Types.Zone
import Micro.Squads.Goals.GoalDefendZone
import Micro.Squads.Squad
import Planning.Plan
import ProxyBwapi.UnitInfo.ForeignUnitInfo

class DefendZone(zone: Zone) extends Plan {
  
  val squad: Squad = new Squad(this)
  val goal: GoalDefendZone = new GoalDefendZone(zone)
  val conscript: Conscript = new Conscript(squad)
  
  override def getChildren: Iterable[Plan] = Array(conscript)
  
  var enemies: Seq[ForeignUnitInfo] = Seq.empty
  
  override def onUpdate() {
  
    if (enemies.size <= 2 && enemies.forall(e => e.unitClass.isWorker || ! e.canAttack)) {
      return
    }
    
    val ourBase = zone.bases.find(base => base.owner.isUs)
  
    conscript.squad.setGoal(goal)
    conscript.mustFight   = zone.bases.exists(_.owner.isUs)
    conscript.overkill    = if (conscript.mustFight) 1.5 else 2.0
    conscript.enemies     = enemies
    conscript.update()
  }
}
