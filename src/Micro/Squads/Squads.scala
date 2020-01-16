package Micro.Squads

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Squads extends SquadBatching {
  
  //val all: mutable.Set[Squad] = new mutable.HashSet[Squad]
  def all: Seq[Squad] = activeBatch.squads.view

  // TODO: Unused; Delete
  val unitsBySquad: mutable.Map[Squad, mutable.HashSet[FriendlyUnitInfo]] = new mutable.HashMap[Squad, mutable.HashSet[FriendlyUnitInfo]]

  // TODO: Replace usage by Unit.Squad; then Delete
  val squadByUnit: mutable.Map[FriendlyUnitInfo, Squad] = new mutable.HashMap[FriendlyUnitInfo, Squad]

  // TODO: Unused; Delete
  val freelancers: mutable.Set[FriendlyUnitInfo] = new mutable.HashSet[FriendlyUnitInfo]

  def allByPriority: Seq[Squad] = all.sortBy(_.client.priority)

  // TODO: Needs new canonical source of truth
  def units(squad: Squad): Set[FriendlyUnitInfo] = unitsBySquad.get(squad).map(_.toSet).getOrElse(Set.empty)
  
  def reset() {
    all.foreach(squad => squad.previousUnits = squad.units)
    squadByUnit.clear()
    unitsBySquad.clear()
    freelancers.clear()
  }

  // TODO: Delete; to be implemented within the batching
  def assignFreelancers() {
    With.squads.allByPriority.foreach(_.goal.prepareForCandidates())
    RecruitmentLevel.values.foreach(recruitmentLevel =>
      With.squads.allByPriority.foreach(squad => {
        // Copy out freelancers to avoid modifying the set while iterating through it.
        val nextFreelancers = freelancers.toVector
        squad.goal.offer(nextFreelancers, recruitmentLevel)
      }))
  }

  // TODO: Delete; Only used internally
  def removeUnit(unit: FriendlyUnitInfo) {
    freelancers += unit
    squadByUnit.get(unit).foreach(unitsBySquad(_).remove(unit))
    squadByUnit.remove(unit)
  }

  // TODO: Replace usage by Squad; then delete
  def addUnit(squad: Squad, unit: FriendlyUnitInfo) {
    removeUnit(unit)
    squadByUnit(unit) = squad
    unitsBySquad(squad) += unit
    freelancers -= unit
  }
  
  def update() {
    assignFreelancers()
    allByPriority.foreach(_.update())
  }
}
