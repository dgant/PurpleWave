package Planning.Plans.GamePlans.Protoss.Situational

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class Nexus2GateThenCannons extends ProposePlacement {
  override lazy val blueprints: Seq[Blueprint] = {
    val mainZone = With.geography.ourMain.zone
    val naturalBase = With.geography.ourNatural
    val naturalZone = naturalBase.zone
    val marginPixels = naturalZone.exit.map(_.pixelCenter.pixelDistance(naturalBase.townHallArea.midPixel) - Protoss.Nexus.radialHypotenuse).getOrElse(128.0)
    val output = Vector(
      new Blueprint(Protoss.Pylon),
      new Blueprint(Protoss.Pylon,        requireZone = Some(naturalZone), placement = Some(PlacementProfiles.defensive), marginPixels = Some(marginPixels - 96.0)),
      new Blueprint(Protoss.PhotonCannon, requireZone = Some(naturalZone), placement = Some(PlacementProfiles.defensive), marginPixels = Some(marginPixels)),
      new Blueprint(Protoss.PhotonCannon, requireZone = Some(naturalZone), placement = Some(PlacementProfiles.defensive), marginPixels = Some(marginPixels)),
      new Blueprint(Protoss.PhotonCannon, requireZone = Some(naturalZone), placement = Some(PlacementProfiles.defensive), marginPixels = Some(marginPixels)))
    output
  }
}

