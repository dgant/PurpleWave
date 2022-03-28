package Tactics.Production

import Lifecycle.With
import ProxyBwapi.BuildableType
import Tactics.Tactic
import Utilities.CountMap

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Produce extends Tactic {

  private var _queueLast: ListBuffer[Production] = ListBuffer.empty
  private var _queueNext: ListBuffer[Production] = ListBuffer.empty

  def queue: Seq[Production] = _queueNext

  def launch() {
    _queueLast = _queueNext
    _queueNext = ListBuffer.empty
    val requests      = With.macroSim.queue
    val typesCounted  = new CountMap[BuildableType]
    val typesNeeded   = new CountMap[BuildableType]
    val matched       = new mutable.HashSet[Production]

    // Requeue any paid-for production, in the same order
    _queueNext ++= _queueLast.view.filter(_.hasSpent)

    // Retain required production or add new production
    requests.foreach(request => {
      val       existingNext = _queueNext.view                        .find(p => p.buildable.satisfies(request) && ! matched.contains(p))
      lazy val  existingLast = _queueLast.view.filterNot(_.hasSpent)  .find(p => p.buildable.satisfies(request) && ! matched.contains(p))
      existingNext.foreach(matched+=)
      if (existingNext.isEmpty) {
        existingLast.foreach(_queueNext+=)
        existingLast.foreach(matched+=)
        if (existingLast.isEmpty) {
          val newProduction = request.makeProduction
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
