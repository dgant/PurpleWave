package Micro.Squads

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

  def update() {
    updateBatching()
    allByPriority.foreach(_.update())
  }
}
