package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plan
import Planning.Plans.Gameplans.All.MacroActions
import ProxyBwapi.Races.Zerg

class ExtractorTrick extends Plan with MacroActions {
  
  override def onUpdate(): Unit = {
    
    lazy val extractors = With.units.ours.filter(e => e.is(Zerg.Extractor) && ! e.complete)
    val shouldBuildExtractor = (
      With.self.supplyTotal400 == 18
      && Vector(17, 18).contains(With.self.supplyUsed400)
      && With.self.minerals >= 84
      && With.units.existsOurs(Zerg.Larva)
      && extractors.isEmpty)
  
    lazy val shouldCancelExtractor = (
      // Give time for our supply to update
      extractors.exists(e => With.framesSince(e.frameDiscovered) > 24)
      && (
        extractors.exists(_.remainingCompletionFrames < 3 * 24)
        || (With.self.supplyTotal400 == 18 && With.self.supplyUsed400 >= 18))
      )
    
    if (shouldBuildExtractor) {
      With.scheduler.request(this, Get(1, Zerg.Extractor))
    } else if (shouldCancelExtractor) {
      cancel(Zerg.Extractor)
    }
  }
  
}
