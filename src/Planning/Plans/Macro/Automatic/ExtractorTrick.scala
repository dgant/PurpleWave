package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Plan
import ProxyBwapi.Races.Zerg

class ExtractorTrick extends Plan {
  
  val extractors = new LockUnits
  extractors.unitMatcher.set(Zerg.Extractor)
  
  override def onUpdate() {
    
    val shouldBuildExtractor = (
      With.self.supplyTotal == 18
      && With.self.supplyUsed == 18
      && With.self.minerals >= 76
      && ! With.units.existsOurs(Zerg.Extractor)
    )
  
    lazy val shouldCancelExtractor = (
      With.self.supplyTotal == 18
      && With.self.supplyUsed == 18
      && With.units.existsOurs(Zerg.Extractor)
    )
    
    if (shouldBuildExtractor) {
      With.scheduler.request(this, RequestAtLeast(1, Zerg.Extractor))
    }
    else if (shouldCancelExtractor) {
      extractors.acquire(this)
      extractors.units.foreach(unit => {
        val intent = new Intention
        intent.canCancel = true
        unit.agent.intend(this, intent)
      })
    }
  }
  
}
