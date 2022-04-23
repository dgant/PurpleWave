package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Placement.Access.{PlaceLabels, PlacementQuery}
import Planning.Plan
import Planning.Plans.GamePlans.MacroActions
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClasses.UnitClass

class BuildZergStaticDefenseAtBases(towersRequired: Int, towerClass: UnitClass) extends Plan with MacroActions {
  
  override def onUpdate(): Unit = {
    val bases = eligibleBases
    if (bases.nonEmpty) {
      bases.foreach(buildInBase)
    }
  }
  
  protected def eligibleBases: Iterable[Base] = With.geography.ourBasesAndSettlements

  protected def makePlacement(unitClass: UnitClass, base: Base): PlacementQuery = {
    val output = new PlacementQuery(unitClass)
    output.requirements.base = Vector(base)
    output.preferences.labelYes = Vector(PlaceLabels.Defensive)
    output
  }
  
  private def buildInBase(base: Base): Unit = {
    val placement = makePlacement(Zerg.CreepColony, base)
    val sunkensInZone = With.units.ours.filter(Zerg.SunkenColony).count(u => placement.acceptExisting(u.tileTopLeft))
    get(towersRequired - sunkensInZone, Zerg.CreepColony, placement)
  }
}
