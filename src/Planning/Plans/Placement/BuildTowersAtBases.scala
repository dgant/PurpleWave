package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Placement.Access.{PlaceLabels, PlacementQuery}
import Planning.Plan
import Planning.Plans.GamePlans.MacroActions
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitClasses.UnitClass

class BuildTowersAtBases(towersRequired: Int, towerClass: UnitClass = Protoss.PhotonCannon) extends Plan with MacroActions {

  override def onUpdate(): Unit = {
    val bases = eligibleBases

    if (bases.nonEmpty) {
      if (towerClass == Protoss.PhotonCannon) {
        get(Protoss.Forge)
      } else if (towerClass == Terran.MissileTurret) {
        get(Terran.EngineeringBay)
      }
    }

    bases.foreach(towerBase)
  }

  protected def eligibleBases: Iterable[Base] = With.geography.ourBasesAndSettlements

  protected def makePlacement(unitClass: UnitClass, base: Base): PlacementQuery = {
    val output = new PlacementQuery(unitClass)
    output.requirements.base = Vector(base)
    output.preferences.label = Vector(PlaceLabels.Defensive)
    output
  }

  private def towerBase(base: Base): Unit = {
    if (towerClass.requiresPsi) {
      get(1, Protoss.Pylon, makePlacement(Protoss.Pylon, base))
    }
    get(towersRequired, towerClass, makePlacement(towerClass, base))
  }
}
