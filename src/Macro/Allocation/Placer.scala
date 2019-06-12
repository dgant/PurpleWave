package Macro.Allocation

import Macro.Architecture.PlacementStates.{PlacementState, PlacementStateInitial}
import Macro.Architecture.{Blueprint, Placement}
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable

object Placer {

  // Designed to be compatible with the legacy (CIG 2017) API for placement
  val queue: mutable.Queue[Blueprint] = new mutable.Queue[Blueprint]
  var placements: Map[Blueprint, Placement] = Map.empty
  def setState(newState: PlacementState): Unit = placementState = newState

  private var placementState: PlacementState = new PlacementStateInitial

  def place(unitClasses: UnitClass*): Unit = {
    placeBlueprints(unitClasses.map(unitClass => new Blueprint(building = Some(unitClass))): _*)
  }
  def placeBlueprints(blueprints: Blueprint*): Map[Blueprint, Placement] = {
    placements = Map.empty
    queue.clear()
    queue ++= blueprints
    while (queue.nonEmpty) {
      placementState = new PlacementStateInitial
      while (placementState.isComplete) {
        placementState.step()
      }
    }
    placements
  }
}
