package Tactics.Production

import Lifecycle.With
import Macro.Buildables.Buildable
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyFor, LockUnits}
import Planning.UnitCounters.CountOne
import Planning.UnitPreferences.PreferIdle
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass

class ResearchTech(buildableTech: Buildable) extends Production {

  setBuildable(buildableTech)
  val tech          : Tech          = buildable.tech.get
  val techerClass   : UnitClass     = tech.whatResearches
  val currencyLock  : LockCurrency  = new LockCurrencyFor(this, tech, 1)
  val techers       : LockUnits     = new LockUnits(this)
  techers.matcher    = u => techerClass(u) && u.techProducing.forall(tech==)
  techers.counter    = CountOne
  techers.preference = PreferIdle

  override def isComplete: Boolean = tech(With.self)
  override def hasSpent: Boolean = techers.units.exists(_.techProducing.contains(tech))

  override def onUpdate() {
    if (isComplete) return
    currencyLock.framesPreordered = Math.max(
      Maff.max(techers.units.map(_.remainingOccupationFrames)).getOrElse(0),
      With.projections.unit(techerClass))
    if (hasSpent || currencyLock.acquire()) {
      techers.acquire()
      techers.units.foreach(_.intend(this, new Intention { toTech = Some(tech) }))
    }
  }
}