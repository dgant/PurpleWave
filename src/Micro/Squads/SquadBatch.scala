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
  private var freelancersUnassigned = new mutable.PriorityQueue[(FriendlyUnitInfo, Double)]()(Ordering.by(_._2))

  def step(): Unit = {
    if ( ! started) {
      eligibleSquads ++= squads.distinct.map(new SquadAssignment(_))
      freelancersUnassigned ++= freelancers.distinct.map(freeLancer => (freeLancer, bestValueOfFreelancer(freeLancer)))
      started = true
    }

    if (freelancersUnassigned.isEmpty) {
      // TODO: Disqualify incomplete squads and release their units before finishing
      val incompleteSquad = eligibleSquads.reverseIterator.find(squad => false)
      if (incompleteSquad.isDefined) {
        freelancersUnassigned ++= incompleteSquad.get.units.map(u => (u, bestValueOfFreelancer(u)))
        eligibleSquads -= incompleteSquad.get
        return
      }

      finished = true
      return
    }

    val freelancer = freelancersUnassigned.dequeue()._1
    var squadsEligibleForUnit = eligibleSquads.filter(_.squad.goal.candidateWelcome(this, freelancer))

    if (squadsEligibleForUnit.isEmpty) {
      //This is happening a lot and producing log spam
      //With.logger.warn("No eligible squads accept " + freelancer + ". Eligible squads remaining: " + eligibleSquads + " out of total squads " + squads)
      return
    }

    // Assign the front freelancer
    val squadScores = squadsEligibleForUnit.map(s => (s, (squadValue(freelancer, s))))
    val bestSquad = squadScores.maxBy(_._2)._1
    bestSquad.addUnit(freelancer)
  }

  def bestValueOfFreelancer(freelancer: FriendlyUnitInfo): Double = {
    ByOption.max(
      eligibleSquads.view
        .filter(_.squad.goal.candidateWelcome(this, freelancer))
        .map(s => s.squad.goal.candidateValue(this, freelancer)))
      .getOrElse(0.0)
  }

  def squadValue(freelancer: FriendlyUnitInfo, squad: SquadAssignment): Double = {
    val inherentValue = squad.squad.goal.inherentValue
    val candidateValue = squad.squad.goal.candidateValue(this, freelancer)
    val output = inherentValue * candidateValue
    output
  }

  def apply(): Unit = {
    With.squads.all.foreach(squad => squad.clearFreelancers())
    assignments.foreach(assignment => assignment.squad.addFreelancers(assignment.units))
  }
}
