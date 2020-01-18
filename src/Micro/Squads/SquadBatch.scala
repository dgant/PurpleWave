package Micro.Squads

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class SquadBatch {

  // TODO: Wrap these; don't try to access them externally
  val squads = new mutable.ArrayBuffer[Squad]
  val freelancers = new mutable.ArrayBuffer[FriendlyUnitInfo]

  def processingStarted: Boolean = started
  def processingFinished: Boolean = finished

  private var started: Boolean = false
  private var finished: Boolean = false

  private var eligibleSquads = new mutable.ListBuffer[SquadAssignment]
  private var freelancersUnassigned = new mutable.Queue[FriendlyUnitInfo]

  def step(): Unit = {
    if ( ! started) {
      eligibleSquads ++= squads.distinct.map(new SquadAssignment(_))
      freelancersUnassigned ++= freelancers.distinct
      started = true

      // TODO: Sort the freelancers by max(Proximity*SquadValue*max(QualityNeed))
      freelancersUnassigned.sortBy(f => f.id)
    }

    if (freelancersUnassigned.isEmpty) {
      val incompleteSquad = eligibleSquads.reverseIterator.find(squad => false) // TODO: Disqualify incomplete squads and release their units before finishing
      if (incompleteSquad.isDefined) {
        freelancersUnassigned ++= incompleteSquad.get.units
        eligibleSquads -= incompleteSquad.get
        return
      }

      finished = true
      return
    }

    if (eligibleSquads.isEmpty) {
      With.logger.warn("Ran out of eligible squads with " + freelancersUnassigned.length + " unassigned freelancers")
      finished = true
      return
    }

    // Assign the front freelancer
    val freelancer = freelancersUnassigned.dequeue()
    eligibleSquads.maxBy(squadValue(freelancer, _)).addUnit(freelancer)
  }

  def squadValue(freelancer: FriendlyUnitInfo, squad: SquadAssignment): Double = {
    1.0 // TODO: Calculate max(Proximity*SquadValue*max(QualityNeed))
  }
}
