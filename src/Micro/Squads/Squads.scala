package Micro.Squads

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Squads {
  
  val all: mutable.Set[Squad] = new mutable.HashSet[Squad]
  val unitsBySquad: mutable.Map[Squad, mutable.HashSet[FriendlyUnitInfo]] = new mutable.HashMap[Squad, mutable.HashSet[FriendlyUnitInfo]]
  val squadByUnit: mutable.Map[FriendlyUnitInfo, Squad] = new mutable.HashMap[FriendlyUnitInfo, Squad]
  val freelancers: mutable.Set[FriendlyUnitInfo] = new mutable.HashSet[FriendlyUnitInfo]
  
  def allByPriority: Seq[Squad] = all.toSeq.sortBy(_.client.priority)
  def units(squad: Squad): Set[FriendlyUnitInfo] = unitsBySquad.get(squad).map(_.toSet).getOrElse(Set.empty)
  def squad(unit: FriendlyUnitInfo): Option[Squad] = squadByUnit.get(unit)
  
  def reset() {
    all.foreach(squad => squad.previousUnits = squad.units)
    all.clear()
    squadByUnit.clear()
    unitsBySquad.clear()
    freelancers.clear()
  }
  
  def addFreelancer(unit: FriendlyUnitInfo) {
    freelancers += unit
  }
  
  def assignFreelancers() {
    RecruitmentLevel.values.foreach(recruitmentLevel =>
      With.squads.allByPriority.foreach(squad => {
        // Copy out freelancers to avoid modifying the set while iterating through it.
        val nextFreelancers = freelancers.toVector
        squad.goal.offer(nextFreelancers, recruitmentLevel)
      }))
  }
  
  def commission(squad: Squad) {
    all += squad
    unitsBySquad.put(squad, new mutable.HashSet[FriendlyUnitInfo])
  }
  
  def removeUnit(unit: FriendlyUnitInfo) {
    freelancers += unit
    squadByUnit.get(unit).foreach(unitsBySquad(_).remove(unit))
    squadByUnit.remove(unit)
  }
  
  def addUnit(squad: Squad, unit: FriendlyUnitInfo) {
    removeUnit(unit)
    squadByUnit(unit) = squad
    unitsBySquad(squad) += unit
    freelancers -= unit
  }
  
  def update() {
    allByPriority.foreach(_.update())
  }
}
