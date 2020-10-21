package Mathematics.Formations.Designers

import Mathematics.Formations.FormationAssigned
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.CountMap

class FormationPush(airDestination: Pixel) extends FormationDesigner {

  override def form(units: Seq[FriendlyUnitInfo]): FormationAssigned = {
    if (units.size < 3) {
      return new FormationAssigned(units.map(u => (u, airDestination)).toMap)
    }

    /*
      Identify destination
      Identify threat hull

     */

    val groundDestination = airDestination.nearestWalkableTile.pixelCenter


    val targets = new CountMap[Pixel]
    units.foreach(u => u.battle.foreach(b => targets.add(b.enemy.vanguard, u.subjectiveValue.toInt)))
    val target = targets.mode.getOrElse(groundDestination)
    null
  }
}
