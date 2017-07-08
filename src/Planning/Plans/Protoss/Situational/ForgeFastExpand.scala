package Planning.Plans.Protoss.Situational

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class ForgeFastExpand extends ProposePlacement {
  override lazy val blueprints = Vector(
    new Blueprint(this, argMargin = Some(false), argPlacement = Some(PlacementProfiles.naturalCannonPylon), building = Some(Protoss.Pylon),         zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(0.0)),
    new Blueprint(this, argMargin = Some(false), argPlacement = Some(PlacementProfiles.naturalCannonPylon), building = Some(Protoss.Forge),         zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(0.0)),
    new Blueprint(this, argMargin = Some(false), argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(2.0 * 32.0)),
    new Blueprint(this, argMargin = Some(false), argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(2.0 * 32.0)),
    new Blueprint(this, argMargin = Some(false), argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(2.0 * 32.0)),
    new Blueprint(this, argMargin = Some(false), argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(2.0 * 32.0)),
    new Blueprint(this, argMargin = Some(false), argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(3.0 * 32.0)),
    new Blueprint(this, argMargin = Some(false), argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(3.0 * 32.0)),
    new Blueprint(this, argMargin = Some(false), argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(3.0 * 32.0)),
    new Blueprint(this, argMargin = Some(false), argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(3.0 * 32.0)))
}

