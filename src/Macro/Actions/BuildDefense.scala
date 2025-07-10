package Macro.Actions

import Lifecycle.With
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass

object BuildDefense extends MacroActions {

  def buildDefensePrerequisites(defenseClass: UnitClass): Unit = {
    if (defenseClass == Terran.MissileTurret)  get(Terran.EngineeringBay)
    if (defenseClass == Protoss.PhotonCannon)  get(Protoss.Forge)
    if (defenseClass == Zerg.SporeColony)      get(Zerg.EvolutionChamber)
  }

  def apply(count: Int, defenseClass: UnitClass, query: UnitClass => PlacementQuery): Unit = {
    if (count > 0) {
      buildDefensePrerequisites(defenseClass)
    }

    if (defenseClass.requiresPsi) {
      get(Protoss.Pylon, query(Protoss.Pylon))
    }

    get(count, defenseClass, query(defenseClass))

    if (defenseClass.whatBuilds._1 == Zerg.CreepColony) {

      val existingDefense = With.units.ours.view.count(u => defenseClass(u) && query(defenseClass).acceptExisting(u.tileTopLeft))

      val requiredCreeps = count - existingDefense

      if (requiredCreeps > 0) {
        get(requiredCreeps, Zerg.CreepColony, query(Zerg.CreepColony))
      }
    }
  }
}
