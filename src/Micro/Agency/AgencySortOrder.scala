package Micro.Agency

import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait AgencySortOrder {
  val unit: FriendlyUnitInfo

  var categoryOrder : Double = 0
  var threatOrder   : Double = 0
  var sortOrder     : Double = 0

  // This is for sorting in ascending order, so lower = more important
  def updateSortOrder(): Unit = {
    threatOrder = unit.matchups.pixelsToThreatRange.getOrElse(1e5)

    categoryOrder = unit.unitClass.agencySortOrder
    if (unit.intent.toBuild.nonEmpty) {
      categoryOrder = 1e6
    } else if (unit.intent.toRepair.isDefined || (Terran.SCV(unit) && unit.orderTarget.exists(u => u.isOurs && u.unitClass.isMechanical))) {
      categoryOrder = 3e6
    } else if (unit.intent.toScoutTiles.nonEmpty) {
      categoryOrder = 9e6
    }
    sortOrder = threatOrder + categoryOrder
  }
}
