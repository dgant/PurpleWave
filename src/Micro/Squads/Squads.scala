package Micro.Squads

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Squads {
  
  val all: mutable.Set[Squad] = new mutable.HashSet[Squad]
  val unitsBySquad: mutable.Map[Squad, mutable.HashSet[FriendlyUnitInfo]] = new mutable.HashMap[Squad, mutable.HashSet[FriendlyUnitInfo]]
  val squadByUnit: mutable.Map[FriendlyUnitInfo, Squad] = new mutable.HashMap[FriendlyUnitInfo, Squad]
  
  def allByPriority: Seq[Squad] = all.toSeq.sortBy(_.client.priority)
  def units(squad: Squad): Set[FriendlyUnitInfo] = unitsBySquad.get(squad).map(_.toSet).getOrElse(Set.empty)
  def squad(unit: FriendlyUnitInfo): Option[Squad] = squadByUnit.get(unit)
  
  def reset() {
    all.clear()
    squadByUnit.clear()
    unitsBySquad.clear()
  }
  
  def commission(squad: Squad) {
    all += squad
    unitsBySquad.put(squad, new mutable.HashSet[FriendlyUnitInfo])
  }
  
  def removeUnit(unit: FriendlyUnitInfo) {
    squadByUnit.get(unit).foreach(unitsBySquad(_).remove(unit))
    squadByUnit.remove(unit)
  }
  
  def addUnit(squad: Squad, unit: FriendlyUnitInfo) {
    removeUnit(unit)
    squadByUnit(unit) = squad
    unitsBySquad(squad) += unit
  }
  
  def update() {
    allByPriority.foreach(_.update())
  }
}
