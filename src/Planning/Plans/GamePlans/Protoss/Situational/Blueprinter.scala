package Planning.Plans.GamePlans.Protoss.Situational

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Planning.Plan
import ProxyBwapi.Races.Protoss

object Blueprinter {
  
  def pylonsAndCannonsAtNatural(
    plan: Plan,
    pylonCount  : Int,
    cannonCount : Int)
  : Seq[Blueprint]  = {
    
    val naturalBase   = With.geography.ourNatural
    val naturalZone   = naturalBase.zone
    val marginPixels  = Math.max(0, naturalZone.exit.map(_.pixelCenter.pixelDistance(naturalBase.townHallArea.midPixel) - Protoss.Nexus.radialHypotenuse).getOrElse(128.0))
    
    val pylons = (0 to pylonCount).map(i =>
      new Blueprint(plan,
        building      = Some(Protoss.Pylon),
        requireZone   = Some(naturalZone),
        placement     = Some(PlacementProfiles.defensive),
        marginPixels  = Some(Math.max(0, marginPixels - 72.0))))
    
    val cannons = (0 to cannonCount).map(i =>
      new Blueprint(plan,
        building      = Some(Protoss.PhotonCannon),
        requireZone   = Some(naturalZone),
        placement     = Some(PlacementProfiles.defensive),
        marginPixels  = Some(marginPixels)))
    
    val output = pylons ++ cannons
    
    output
  }
}
