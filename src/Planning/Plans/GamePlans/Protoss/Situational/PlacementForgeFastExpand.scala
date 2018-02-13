package Planning.Plans.GamePlans.Protoss.Situational

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class PlacementForgeFastExpand extends ProposePlacement {
  override lazy val blueprints: Seq[Blueprint] = {
    val mainZone      = With.geography.ourMain.zone
    val naturalBase   = With.geography.ourNatural
    val naturalZone   = naturalBase.zone
    val marginPixels  = naturalZone.exit.map(_.pixelCenter.pixelDistanceFast(naturalBase.townHallArea.midPixel) - Protoss.Nexus.radialHypotenuse).getOrElse(128.0)
    val output = Vector(
      new Blueprint(this, building = Some(Protoss.Pylon),           requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.wallPylon),    marginPixels = Some(marginPixels - 96.0)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.wallCannon),   marginPixels = Some(marginPixels)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.wallCannon),   marginPixels = Some(marginPixels)),
      new Blueprint(this, building = Some(Protoss.Forge),                                             placement = Some(PlacementProfiles.hugTownHall),  marginPixels = Some(marginPixels - 96.0)),
      new Blueprint(this, building = Some(Protoss.Gateway),                                           placement = Some(PlacementProfiles.hugTownHall),  marginPixels = Some(marginPixels - 64.0)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.wallCannon),   marginPixels = Some(marginPixels)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.wallCannon),   marginPixels = Some(marginPixels)),
      new Blueprint(this, building = Some(Protoss.Pylon),           requireZone = Some(mainZone)),
      new Blueprint(this, building = Some(Protoss.Pylon),           preferZone  = Some(naturalZone),  placement = Some(PlacementProfiles.wallCannon),   marginPixels = Some(marginPixels - 96.0)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.wallCannon),   marginPixels = Some(marginPixels)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.wallCannon),   marginPixels = Some(marginPixels)),
      new Blueprint(this, building = Some(Protoss.CyberneticsCore), preferZone  = Some(mainZone))
    )
    output
  }
}

