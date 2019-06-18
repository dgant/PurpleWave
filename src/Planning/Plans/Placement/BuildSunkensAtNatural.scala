package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import ProxyBwapi.Races.Zerg

class BuildSunkensAtNatural(
  towersRequired: Int,
  placement: PlacementProfile = PlacementProfiles.defensive)
  extends BuildZergStaticDefenseAtBases(
    Zerg.SunkenColony,
    towersRequired,
    placement) {
  
  override def eligibleBases: Iterable[Base] = Vector(With.geography.ourNatural)
}