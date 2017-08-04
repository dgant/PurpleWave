package Planning.Plans.Army

import Information.Geography.Types.Zone
import Lifecycle.With
import Micro.Squads.Goals.ProtectZone
import Planning.Plan
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, UnitInfo}

class ControlZone(zone: Zone) extends Plan {
  
  val recruit: Conscript = new Conscript
  val goal: ProtectZone = new ProtectZone(zone)
  
  override def getChildren: Iterable[Plan] = Array(recruit)
  
  var enemies: Seq[ForeignUnitInfo] = Seq.empty
  
  override def onUpdate() {
  
    val ourBase = zone.bases.find(base => base.owner.isUs)
    
    enemies = With.units.enemy.filter(threateningZone).toSeq
  
    recruit.squad.goal  = goal
    recruit.mustFight   = zone.bases.exists(_.owner.isUs)
    recruit.overkill    = if (recruit.mustFight) 1.5 else 2.0
    recruit.enemies     = enemies
    recruit.update()
  }
  
  def threateningZone(enemy: UnitInfo): Boolean = {
    val timeThreshold = 24 * 8
    val unitZone = enemy.pixelCenter.zone
    zone == unitZone ||
    (enemy.flying && enemy.framesToTravelTo(zone.centroid.pixelCenter) < timeThreshold) ||
    zone.edges.exists(edge => enemy.framesToTravelTo(edge.centerPixel) < timeThreshold)
  }
}
