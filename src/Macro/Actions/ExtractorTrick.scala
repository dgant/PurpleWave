package Macro.Actions

import Lifecycle.With
import Macro.Requests.Get
import ProxyBwapi.Races.Zerg
import Utilities.In

object ExtractorTrick extends MacroActions {
  def apply(): Unit = {
    lazy val extractors = With.units.ours.filter(e => Zerg.Extractor(e) && ! e.complete)
    val shouldBuildExtractor = (
      With.self.supplyTotal400 == 18
        && In(With.self.supplyUsed400, 17, 18)
        && With.self.minerals >= 84
        && have(Zerg.Larva)
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
