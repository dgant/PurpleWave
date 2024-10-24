package Tactic.Production

import Lifecycle.With
import Macro.Requests.RequestBuildable
import Planning.ResourceLocks.{LockCurrency, LockCurrencyFor, LockUnits}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.UnitCounters.CountOne
import Utilities.UnitPreferences.PreferIdle

class ProduceTech(requestArg: RequestBuildable, expectedFramesArg: Int) extends Production {
  setRequest(requestArg, expectedFramesArg)
  val tech          : Tech          = request.tech.get
  val techerClass   : UnitClass     = tech.whatResearches
  val currencyLock  : LockCurrency  = new LockCurrencyFor(this, tech)
  val techers       : LockUnits     = new LockUnits(this,
    (u: UnitInfo) => techerClass(u) && u.techProducing.forall(tech==),
    PreferIdle,
    CountOne)

  override def isComplete: Boolean = tech(With.self)
  override def hasSpent: Boolean = techers.units.exists(_.techProducing.contains(tech))

  override def onUpdate(): Unit = {
    if (hasSpent || currencyLock.acquire()) {
      techers.acquire()
      techers.units.foreach(_.intend(this).setTech(tech))
    }
  }
}
