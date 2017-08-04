package Micro.Squads

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Squads {
  
  var all: mutable.Set[Squad] = new mutable.HashSet[Squad]
  var squadByUnit: mutable.Map[FriendlyUnitInfo, Squad] = new mutable.HashMap[FriendlyUnitInfo, Squad]
  
  def squadsByPriority: Seq[Squad] = all.toSeq.sortBy(_.client.priority)
  
  def reset() {
    all.clear()
    squadByUnit.clear()
  }
  
  def commission(squad: Squad) {
    all += squad
    squad.recruits.foreach(squadByUnit(_) = squad)
  }
  
  def addUnit(squad: Squad, unit: FriendlyUnitInfo) {
    squadByUnit(unit) = squad
  }
  
  def update() {
    all.foreach(_.update())
  }
}
