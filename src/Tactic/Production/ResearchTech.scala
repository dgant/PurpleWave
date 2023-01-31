package Tactic.Production

import Lifecycle.With
import Macro.Requests.RequestBuildable
import Planning.ResourceLocks.{LockCurrency, LockCurrencyFor, LockUnits}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.UnitCounters.CountOne
import Utilities.UnitPreferences.PreferIdle

class ResearchTech(requestArg: RequestBuildable, expectedFramesArg: Int) extends Production {
  setRequest(requestArg, expectedFramesArg)
  val tech          : Tech          = request.tech.get
  val techerClass   : UnitClass     = tech.whatResearches
  val currencyLock  : LockCurrency  = new LockCurrencyFor(this, tech, 1)
  val techers       : LockUnits     = new LockUnits(this)
  techers.matcher    = u => techerClass(u) && u.techProducing.forall(tech==)
  techers.counter    = CountOne
  techers.preference = PreferIdle

  override def isComplete: Boolean = tech(With.self)
  override def hasSpent: Boolean = techers.units.exists(_.techProducing.contains(tech))

  override def onUpdate(): Unit = {
    if (hasSpent || currencyLock.acquire()) {
      techers.acquire()
      techers.units.foreach(_.intend(this).setTech(tech))
    }
  }
}
