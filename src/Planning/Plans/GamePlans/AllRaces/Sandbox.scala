package Planning.Plans.GamePlans.AllRaces

import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.GamePlans.GameplanTemplate
import ProxyBwapi.Races.Zerg

class Sandbox extends GameplanTemplate {

  override def buildOrder: Seq[BuildRequest] = Seq(
    Get(9, Zerg.Drone),
    Get(2, Zerg.Overlord),
    Get(12, Zerg.Drone),
    Get(2, Zerg.Hatchery),
    Get(Zerg.Extractor),
    Get(Zerg.SpawningPool),
    Get(14, Zerg.Drone),
    Get(6, Zerg.Zergling),
    Get(Zerg.Lair),
    Get(8, Zerg.Zergling),
    Get(Zerg.ZerglingSpeed),
    Get(12, Zerg.Zergling),
    Get(Zerg.Spire),
    Get(13, Zerg.Zergling),
    Get(2, Zerg.Extractor),
    Get(14, Zerg.Zergling),
    Get(6, Zerg.Mutalisk)
  )

  override def buildPlans: Seq[Plan] = Seq(
  )
}
