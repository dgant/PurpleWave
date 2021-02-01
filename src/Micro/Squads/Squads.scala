package Micro.Squads

class Squads extends SquadBatcher {

  def all: Seq[Squad] = activeBatch.squads.view

  def updateGoals() {
    all.foreach(_.run())
  }

  def clearConscripts(): Unit = {
    all.foreach(_.clearConscripts())
  }
}
