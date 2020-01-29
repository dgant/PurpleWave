package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Buildables.{Buildable, BuildableTech}
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyForTech, LockUnits}
import Planning.UnitCounters.UnitCountOne
import Planning.UnitPreferences.UnitPreferIdle
import ProxyBwapi.Techs.Tech
import Utilities.ByOption

class ResearchTech(tech: Tech) extends ProductionPlan {

  override def producerCurrencyLocks: Seq[LockCurrency] = Seq(currencyLock)
  override def producerUnitLocks: Seq[LockUnits] = Seq(techers)
  override def producerInProgress: Boolean = techers.units.exists(_.techProducing.contains(tech))
  override def buildable: Buildable = BuildableTech(tech)

  val techerClass = tech.whatResearches
  val currencyLock = new LockCurrencyForTech(tech)
  val techers = new LockUnits {
    unitCounter.set(UnitCountOne)
    unitMatcher.set(techerClass)
    unitPreference.set(UnitPreferIdle)
  }
  
  description.set("Tech " + tech)
  
  override def isComplete: Boolean = With.self.hasTech(tech)
  
  override def onUpdate() {
    if (isComplete) return
  
    // Don't even stick a projected expenditure in the queue if we're this far out.
    if ( ! With.units.existsOurs(techerClass)) return
    
    currencyLock.framesPreordered = Math.max(
      ByOption.max(techers.units.map(_.remainingOccupationFrames)).getOrElse(0),
      With.projections.unit(techerClass))
    currencyLock.acquire(this)
    currencyLock.isSpent = With.units.ours.exists(techer => techer.teching && techer.techingType == tech)
    if ( ! currencyLock.satisfied) return
  
    techers.acquire(this)
    techers.units.foreach(_.agent.intend(this, new Intention { toTech = Some(tech) }))
  }
}
