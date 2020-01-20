package Micro.Squads

class Squads extends SquadBatcher {

  def all: Seq[Squad] = activeBatch.squads.view
  def allByPriority: Seq[Squad] = all.sortBy(_.client.priority)

  def updateGoals() {
    allByPriority.foreach(_.run())
  }
}
