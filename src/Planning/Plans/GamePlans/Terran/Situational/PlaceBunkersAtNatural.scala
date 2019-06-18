package Planning.Plans.GamePlans.Terran.Situational

import Lifecycle.With
import Macro.Architecture.Blueprint
import Planning.Plans.Placement.ProposePlacement
import ProxyBwapi.Races.Terran

class PlaceBunkersAtNatural(bunkers: Int) extends ProposePlacement {
  override lazy val blueprints: Seq[Blueprint] = (1 to bunkers).map(i =>
    new Blueprint(Terran.Bunker, requireZone = Some(With.geography.ourNatural.zone))
  )
}
