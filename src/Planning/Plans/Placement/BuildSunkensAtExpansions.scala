package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import ProxyBwapi.Races.Zerg

class BuildSunkensAtExpansions(
  towersRequired: Int,
  placement: PlacementProfile = PlacementProfiles.hugWorkersWithCannon)
  extends BuildZergStaticDefenseAtBases(
    Zerg.SunkenColony,
    towersRequired,
    placement) {
  
  override def eligibleBases: Iterable[Base] = {
    With.geography.ourBasesAndSettlements
      .filterNot(_ == With.geography.ourMain)
      .filterNot(_ == With.geography.ourNatural)
  }
}