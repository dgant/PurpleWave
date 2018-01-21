package Planning.Plans.GamePlans.Terran.Situational

import Lifecycle.With
import Macro.Architecture.Blueprint
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Terran

class TvZPlacement extends ProposePlacement {
  override lazy val blueprints: Seq[Blueprint] = Vector(
    new Blueprint(this, building = Some(Terran.Bunker), preferZone = Some(With.geography.ourNatural.zone)),
    new Blueprint(this, building = Some(Terran.MissileTurret), preferZone = Some(With.geography.ourNatural.zone), marginPixels = Some(32.0 * 6.0)),
    new Blueprint(this, building = Some(Terran.Bunker), preferZone = Some(With.geography.ourNatural.zone)),
    new Blueprint(this, building = Some(Terran.Bunker), preferZone = Some(With.geography.ourNatural.zone)),
    new Blueprint(this, building = Some(Terran.Bunker), preferZone = Some(With.geography.ourNatural.zone))
  )
}
