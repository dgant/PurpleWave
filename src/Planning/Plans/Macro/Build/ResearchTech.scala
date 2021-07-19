package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Buildables.{Buildable, BuildableTech}
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyForTech, LockUnits}
import Planning.UnitCounters.CountOne
import Planning.UnitPreferences.PreferIdle
import ProxyBwapi.Techs.Tech

class ResearchTech(tech: Tech) extends Production {

  override def producerCurrencyLocks: Seq[LockCurrency] = Seq(currencyLock)
  override def buildable: Buildable = BuildableTech(tech)

  val techerClass = tech.whatResearches
  val currencyLock = new LockCurrencyForTech(this, tech)
  val techers = new LockUnits(this)
  techers.matcher = techerClass
  techers.counter = CountOne
  techers.preference = PreferIdle
  
  override def isComplete: Boolean = With.self.hasTech(tech)
  
  override def onUpdate() {
    if (isComplete) return
  
    // Don't even stick a projected expenditure in the queue if we're this far out.
    if ( ! With.units.existsOurs(techerClass)) return
    
    currencyLock.framesPreordered = Math.max(
      Maff.max(techers.units.map(_.remainingOccupationFrames)).getOrElse(0),
      With.projections.unit(techerClass))
    currencyLock.acquire()
    currencyLock.isSpent = With.units.ours.exists(techer => techer.teching && techer.techingType == tech)
    if ( ! currencyLock.satisfied) return
  
    techers.acquire()
    techers.units.foreach(_.intend(this, new Intention { toTech = Some(tech) }))
  }

  override def toString: String = f"Research $tech"
}
