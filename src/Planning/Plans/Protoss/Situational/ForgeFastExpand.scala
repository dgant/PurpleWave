package Planning.Plans.Protoss.Situational

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Compound.Parallel
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class ForgeFastExpand extends Parallel {
  
  override def onUpdate(): Unit = {
    children.set(proposals)
    super.onUpdate()
  }
  
  private lazy val proposals =
    Vector(
      new Blueprint(this, argPlacement = Some(PlacementProfiles.cannonPylon),    building = Some(Protoss.Pylon),         zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(5.0 * 32.0)),
      new Blueprint(this, argPlacement = Some(PlacementProfiles.groundDefense),  building = Some(Protoss.Forge),         zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(5.0 * 32.0)),
      new Blueprint(this, argPlacement = Some(PlacementProfiles.groundDefense),  building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(7.0 * 32.0)),
      new Blueprint(this, argPlacement = Some(PlacementProfiles.groundDefense),  building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(7.0 * 32.0)),
      new Blueprint(this, argPlacement = Some(PlacementProfiles.groundDefense),  building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(7.0 * 32.0)),
      new Blueprint(this, argPlacement = Some(PlacementProfiles.groundDefense),  building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(7.0 * 32.0)))
    .map(new ProposePlacement(_))
}
