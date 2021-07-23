package Macro.Scheduling

import Lifecycle.With
import Macro.BuildRequests.BuildRequest
import Macro.Buildables.Buildable
import Mathematics.Maff
import Planning.Prioritized
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Utilities.CountMap

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Scheduler {

  val requests        = new mutable.HashMap[Prioritized, mutable.ArrayBuffer[BuildRequest]]
  val unitsWanted     = new CountMap[UnitClass]
  val unitsCounted    = new CountMap[UnitClass]
  val upgradesCounted = new CountMap[Upgrade]
  val techsCounted    = new mutable.HashSet[Tech]
  val _queue          = new ArrayBuffer[Buildable]

  def queue: Seq[Buildable] = _queue

  def reset() {
    requests.clear()
    unitsWanted.clear()
    unitsCounted.clear()
    upgradesCounted.clear()
    techsCounted.clear()
    _queue.clear
    With.units.ours.foreach(MacroCounter.countComplete(_).foreach(p => unitsCounted(p._1) += p._2))
    Upgrades.all.foreach(u => upgradesCounted(u) = With.self.getUpgradeLevel(u))
    techsCounted ++= Techs.all.filter(With.self.hasTech)
  }

  def request(requester: Prioritized, theRequest: BuildRequest) {
    request(requester, Iterable(theRequest))
  }

  def request(requester: Prioritized, buildRequests: Iterable[BuildRequest]): Unit = {
    requester.prioritize()
    if ( ! requests.contains(requester)) {
      requests(requester) = ArrayBuffer.empty
    }
    requests(requester) ++= buildRequests
    buildRequests.foreach(b => _queue ++= getUnfulfilledBuildables(b))
  }

  def totalRequested[T](item: T): Int = {
    val requestedAmounts = requests.view.flatMap(r => r._2).filter(_.buildable.is(item)).map(_.total)
    if (item.isInstanceOf[UnitClass]) requestedAmounts.sum else Maff.max(requestedAmounts).getOrElse(0)
  }

  def audit: Vector[(Prioritized, Iterable[BuildRequest])] = {
    requests.toVector.sortBy(_._1.priorityUntouched)
  }

  private def getUnfulfilledBuildables(request: BuildRequest): Iterable[Buildable] = {
    if (request.buildable.upgradeOption.nonEmpty) {
      val upgrade = request.buildable.upgradeOption.get
      if (upgradesCounted(upgrade) < request.buildable.upgradeLevel) {
        upgradesCounted(upgrade) = request.buildable.upgradeLevel
        Vector(request.buildable)
      } else None
    } else if (request.buildable.techOption.nonEmpty) {
      val tech = request.buildable.techOption.get
      if (techsCounted.contains(tech)) None else {
        techsCounted += tech
        Vector(request.buildable)
      }
    } else {
      val unit = request.buildable.unitOption.get
      val unitCountActual = unitsCounted(unit)
      unitsWanted.put(unit, Math.max(unitsWanted(unit), request.total))
      var difference = unitsWanted(unit) - unitCountActual
      if (unit.isTwoUnitsInOneEgg) {
        difference = (1 + difference) / 2
      }
      if (difference > 0) {
        unitsCounted(unit) += difference
        val buildables = (0 until difference).map(i => request.buildable)
        buildables
      } else {
        Vector.empty
      }
    }
  }
}
