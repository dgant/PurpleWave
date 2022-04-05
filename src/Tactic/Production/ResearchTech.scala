package Tactic.Production

import Lifecycle.With
import Macro.Requests.RequestBuildable
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyFor, LockUnits}
import Utilities.UnitCounters.CountOne
import Utilities.UnitPreferences.PreferIdle
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass

class ResearchTech(requestArg: RequestBuildable) extends Production {
  setRequest(requestArg)
  val tech          : Tech          = request.tech.get
  val techerClass   : UnitClass     = tech.whatResearches
  val currencyLock  : LockCurrency  = new LockCurrencyFor(this, tech, 1)
  val techers       : LockUnits     = new LockUnits(this)
  techers.matcher    = u => techerClass(u) && u.techProducing.forall(tech==)
  techers.counter    = CountOne
  techers.preference = PreferIdle

  override def isComplete: Boolean = tech(With.self)
  override def hasSpent: Boolean = techers.units.exists(_.techProducing.contains(tech))

  override def onUpdate() {
    if (hasSpent || currencyLock.acquire()) {
      techers.acquire()
      techers.units.foreach(_.intend(this, new Intention { toTech = Some(tech) }))
    }
  }
}
