package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Scheduling.Project
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.{LockCurrencyForTech, LockUnits}
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Plan
import ProxyBwapi.Techs.Tech

class ResearchTech(tech: Tech) extends Plan {

  val techerClass = tech.whatResearches
  val currency = new LockCurrencyForTech(tech)
  val techers = new LockUnits {
    unitCounter.set(UnitCountOne)
    unitMatcher.set(techerClass)
  }
  
  description.set("Tech " + tech)
  
  override def isComplete: Boolean = With.self.hasTech(tech)
  
  override def onUpdate() {
    if (isComplete) return
    
    currency.framesPreordered = Project.framesToUnits(techerClass)
    currency.acquire(this)
    currency.isSpent = With.units.ours.exists(techer => techer.teching && techer.techingType == tech)
    if ( ! currency.satisfied) return
  
    techers.acquire(this)
    techers.units.foreach(_.agent.intend(this, new Intention { toTech = Some(tech) }))
  }
}
