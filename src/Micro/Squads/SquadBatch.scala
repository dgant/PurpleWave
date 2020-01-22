package Micro.Squads

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

import scala.collection.mutable

class SquadBatch {

  val frameCreated: Int = With.frame
  val squads = new mutable.ArrayBuffer[Squad]
  val freelancers = new mutable.ArrayBuffer[FriendlyUnitInfo]

  def processingStarted: Boolean = started
  def processingFinished: Boolean = finished

  def assignments: Seq[SquadAssignment] = eligibleSquads

  private var started: Boolean = false
  private var finished: Boolean = false

  private var eligibleSquads = new mutable.ListBuffer[SquadAssignment]
  private var freelancersUnassigned = new mutable.Queue[FriendlyUnitInfo]

  def step(): Unit = {
    if ( ! started) {
      eligibleSquads ++= squads.distinct.map(new SquadAssignment(_))
      freelancersUnassigned ++= freelancers.distinct
      started = true

      freelancersUnassigned.sortBy(f => - ByOption.max(
        eligibleSquads.view
          .filter(_.squad.goal.candidateWelcome(this, f))
          .map(s => s.squad.goal.candidateValue(this, f)))
        .getOrElse(0.0))
    }

    if (freelancersUnassigned.isEmpty) {
      // TODO: Disqualify incomplete squads and release their units before finishing
      val incompleteSquad = eligibleSquads.reverseIterator.find(squad => false)
      if (incompleteSquad.isDefined) {
        freelancersUnassigned ++= incompleteSquad.get.units
        eligibleSquads -= incompleteSquad.get
        return
      }

      finished = true
      return
    }

    val freelancer = freelancersUnassigned.dequeue()
    var squadsEligibleForUnit = eligibleSquads.filter(_.squad.goal.candidateWelcome(this, freelancer))

    if (squadsEligibleForUnit.isEmpty) {
      With.logger.warn("No eligible squads accept " + freelancer + ". Eligible squads remaining: " + eligibleSquads)
      return
    }

    // Assign the front freelancer
    val squadScores = squadsEligibleForUnit.map(s => (s, (squadValue(freelancer, s))))
    val bestSquad = squadScores.maxBy(_._2)._1
    bestSquad.addUnit(freelancer)
  }

  def squadValue(freelancer: FriendlyUnitInfo, squad: SquadAssignment): Double = {
    val inherentValue = squad.squad.goal.inherentValue
    val candidateValue = squad.squad.goal.candidateValue(this, freelancer)
    val output = inherentValue * candidateValue
    output
  }

  def apply(): Unit = {
    With.squads.all.foreach(squad => squad.clearUnits())
    assignments.foreach(assignment => assignment.squad.addUnits(assignment.units))
  }
}
