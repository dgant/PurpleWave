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
  val techerLock    : LockUnits     = new LockUnits(this)
  techerLock.matcher    = techerClass
  techerLock.counter    = CountOne
  techerLock.preference = PreferIdle

  override def isComplete: Boolean = tech(With.self)

  override def onUpdate() {
    if (isComplete) return
    currencyLock.framesPreordered = Math.max(
      Maff.max(techerLock.units.map(_.remainingOccupationFrames)).getOrElse(0),
      With.projections.unit(techerClass))
    currencyLock.acquire()
    currencyLock.isSpent = With.units.ours.exists(techer => techer.teching && techer.techingType == tech)
    if ( ! currencyLock.satisfied) return
    techerLock.acquire()
    techerLock.units.foreach(_.intend(this, new Intention { toTech = Some(tech) }))
  }
}
