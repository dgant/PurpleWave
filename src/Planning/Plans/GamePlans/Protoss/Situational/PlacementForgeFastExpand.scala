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
    val marginPixels  = naturalZone.exit.map(_.pixelCenter.pixelDistance(naturalBase.townHallArea.midPixel) - Protoss.Nexus.radialHypotenuse).getOrElse(128.0)
    val output = Vector(
      new Blueprint(this, building = Some(Protoss.Pylon),           requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.defensive), marginPixels = Some(32*4)),
      new Blueprint(this, building = Some(Protoss.Forge),           preferZone  = Some(naturalZone),  placement = Some(PlacementProfiles.defensive), marginPixels = Some(0)),
      new Blueprint(this, building = Some(Protoss.Gateway),         preferZone  = Some(naturalZone),  placement = Some(PlacementProfiles.defensive), marginPixels = Some(0)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.defensive), marginPixels = Some(32*6)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.defensive), marginPixels = Some(32*6)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.defensive), marginPixels = Some(32*6)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.defensive), marginPixels = Some(32*6)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.defensive), marginPixels = Some(32*6)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.defensive), marginPixels = Some(32*6)),
      new Blueprint(this, building = Some(Protoss.Pylon),           requireZone = Some(mainZone)),
      new Blueprint(this, building = Some(Protoss.Pylon),           requireZone = Some(mainZone)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.defensive), marginPixels = Some(96)),
      new Blueprint(this, building = Some(Protoss.PhotonCannon),    requireZone = Some(naturalZone),  placement = Some(PlacementProfiles.defensive), marginPixels = Some(96)),
      new Blueprint(this, building = Some(Protoss.CyberneticsCore), preferZone  = Some(mainZone)),
      new Blueprint(this, building = Some(Protoss.Gateway),         requireZone = Some(mainZone)),
      new Blueprint(this, building = Some(Protoss.Gateway),         requireZone = Some(mainZone)),
      new Blueprint(this, building = Some(Protoss.Gateway),         requireZone = Some(mainZone)),
      new Blueprint(this, building = Some(Protoss.Gateway),         requireZone = Some(mainZone)),
      new Blueprint(this, building = Some(Protoss.Stargate),        requireZone = Some(mainZone)),
      new Blueprint(this, building = Some(Protoss.Stargate),        requireZone = Some(mainZone)),
      new Blueprint(this, building = Some(Protoss.CitadelOfAdun),   requireZone = Some(mainZone)),
      new Blueprint(this, building = Some(Protoss.TemplarArchives), requireZone = Some(mainZone))
    )
    output
  }
}

