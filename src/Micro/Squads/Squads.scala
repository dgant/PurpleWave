package Micro.Squads

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class Squads extends SquadBatching {

  def all: Seq[Squad] = activeBatch.squads.view
  def allByPriority: Seq[Squad] = all.sortBy(_.client.priority)

  // TODO: Replace usage by Unit.Squad; then delete
  val squadByUnit: Map[FriendlyUnitInfo, Squad] = Map.empty

  // TODO: Needs new canonical source of truth
  def units(squad: Squad): Set[FriendlyUnitInfo] = Set.empty

  def reset() {
    startNewBatch()

    // TODO: Do this wherever is newly appropriate
    all.foreach(squad => squad.previousUnits = squad.units)
  }

  // TODO: Delete; to be implemented within the batching
  def assignFreelancers() {
    With.squads.allByPriority.foreach(_.goal.prepareForCandidates())
    RecruitmentLevel.values.foreach(recruitmentLevel =>
      With.squads.allByPriority.foreach(squad => {
        // Copy out freelancers to avoid modifying the set while iterating through it.
        // val nextFreelancers = freelancers.toVector
        // squad.goal.offer(nextFreelancers, recruitmentLevel)
      }))
  }

  // TODO: Replace usage by Squad; then delete
  def addUnit(squad: Squad, unit: FriendlyUnitInfo) {
  }

  def update() {
    updateBatching()
    allByPriority.foreach(_.update())
  }
}
