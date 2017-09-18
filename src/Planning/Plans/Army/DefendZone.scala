package Planning.Plans.Army

import Information.Geography.Types.Zone
import Micro.Squads.Goals.SquadDefendZone
import Planning.Plan
import ProxyBwapi.UnitInfo.ForeignUnitInfo

class DefendZone(zone: Zone) extends Plan {
  
  val conscript: Conscript = new Conscript
  val goal: SquadDefendZone = new SquadDefendZone(zone)
  
  override def getChildren: Iterable[Plan] = Array(conscript)
  
  var enemies: Seq[ForeignUnitInfo] = Seq.empty
  
  override def onUpdate() {
  
    val ourBase = zone.bases.find(base => base.owner.isUs)
  
    conscript.squad.goal  = goal
    conscript.mustFight   = zone.bases.exists(_.owner.isUs)
    conscript.overkill    = if (conscript.mustFight) 1.5 else 2.0
    conscript.enemies     = enemies
    conscript.update()
  }
}
