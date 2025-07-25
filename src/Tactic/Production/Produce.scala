package Tactic.Production

import Lifecycle.With
import Tactic.Tactics.Tactic

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Produce extends Tactic {

  private var _queueLast: ListBuffer[Production] = ListBuffer.empty
  private var _queueNext: ListBuffer[Production] = ListBuffer.empty

  def queue: Seq[Production] = _queueNext
  def queueNextAndLast: Seq[Production] = _queueNext.view ++ _queueLast.view

  def launch(): Unit = {
    _queueLast = _queueNext
    _queueNext = ListBuffer.empty

    // Requeue any paid-for production, in the same order
    _queueNext ++= _queueLast.view.filter(_.hasSpent)

    // Retain required production or add new production
    val matched = new mutable.HashSet[Production]
    With.macroSim.queue.foreach(upcomingRequest => {
      val (request, expectedFrames) = upcomingRequest
      val       existingNext = _queueNext.view                        .find(production => production.satisfies(request) && ! matched.contains(production))
      lazy val  existingLast = _queueLast.view.filterNot(_.hasSpent)  .find(production => production.satisfies(request) && ! matched.contains(production))
      existingNext.foreach(matched+=)
      existingNext.foreach(_.setRequest(request, expectedFrames)) // Cosmetic: Ensures that the quantity of the Production matches the quantity on the Request
      if (existingNext.isEmpty && request.specificTrainee.isEmpty) {
        existingLast.foreach(_queueNext+=)
        existingLast.foreach(matched+=)
        existingLast.foreach(_.setRequest(request, expectedFrames)) // Cosmetic: Ensures that the quantity of the Production matches the quantity on the Request
        if (existingLast.isEmpty) {
          val newProduction = request.makeProduction(expectedFrames)
          _queueNext += newProduction
          matched += newProduction
        }
      }
    })

    // Remove completed production
    // Do this after adding builds to avoid accidentally restarting just-completed builds
    val completed = _queueNext.filter(_.isComplete)
    if (completed.nonEmpty) {
      _queueNext --= completed
    }

    // Execute production
    queue.foreach(_.update())
  }
}
