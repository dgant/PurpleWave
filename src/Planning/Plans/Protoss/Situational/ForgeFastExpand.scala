package Planning.Plans.Protoss.Situational

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class ForgeFastExpand(var cannonsInFront: Boolean) extends ProposePlacement {
  override lazy val blueprints: Iterable[Blueprint] = Vector(
    new Blueprint(this, argWall = Some(true), argMargin = Some(false),  argPlacement = Some(PlacementProfiles.naturalCannonPylon), building = Some(Protoss.Pylon),         zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(if (cannonsInFront) 5.0 else 0.0)),
    new Blueprint(this, argWall = Some(true), argMargin = Some(false),  argPlacement = Some(PlacementProfiles.naturalCannonPylon), building = Some(Protoss.Forge),         zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(if (cannonsInFront) 5.0 else 0.0)),
    new Blueprint(this, argWall = Some(true),                           argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(2.0 * 32.0)),
    new Blueprint(this, argWall = Some(true),                           argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(2.0 * 32.0)),
    new Blueprint(this, argWall = Some(true),                           argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(2.0 * 32.0)),
    new Blueprint(this, argWall = Some(true),                           argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(2.0 * 32.0)),
    new Blueprint(this, argWall = Some(true),                           argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(3.0 * 32.0)),
    new Blueprint(this, argWall = Some(true),                           argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(3.0 * 32.0)),
    new Blueprint(this, argWall = Some(true),                           argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(3.0 * 32.0)),
    new Blueprint(this, argWall = Some(true),                           argPlacement = Some(PlacementProfiles.naturalCannon),      building = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone), argRangePixels = Some(3.0 * 32.0)),
    new Blueprint(this, building = Some(Protoss.Pylon),                 zone = Some(With.geography.ourMain.zone)),
    new Blueprint(this, building = Some(Protoss.Pylon),                 zone = Some(With.geography.ourMain.zone)),
    new Blueprint(this, building = Some(Protoss.Gateway),               zone = Some(With.geography.ourMain.zone)),
    new Blueprint(this, building = Some(Protoss.CyberneticsCore),       zone = Some(With.geography.ourMain.zone))
  )
}

