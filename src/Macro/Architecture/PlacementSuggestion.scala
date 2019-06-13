package Macro.Architecture

import Lifecycle.With
import Mathematics.Points.Tile
import ProxyBwapi.UnitClasses.UnitClass

class PlacementSuggestion(
   val building: UnitClass,
   var tile: Option[Tile],
   val blueprint: Blueprint) {

  def this(unitClass: UnitClass, tile: Tile) {
    this(unitClass, Some(tile), new Blueprint(Some(unitClass)))
  }

  def this(unitClass: UnitClass, blueprint: Blueprint) {
    this(unitClass, None, blueprint)
  }

  def place(): PlacementSuggestion = {
    With.placement.place(this)
    this
  }
}