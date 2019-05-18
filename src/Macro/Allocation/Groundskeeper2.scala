package Macro.Allocation

import Lifecycle.With
import Mathematics.Points.Tile
import Planning.Plan
import Planning.Plans.Basic.NoPlan
import ProxyBwapi.UnitClasses.UnitClass

class Groundskeeper2 {

  def suggest(unitClasses: Seq[UnitClass], tile: Tile): Unit = {

  }

  def getSuggestion(unitClass: UnitClass): Option[Tile] = {
    None
  }

  def reserve(plan: Plan, tiles: Seq[Tile]): Unit = {

  }

  case class Reservation(update: Int, plan: Plan, unitClasses: Seq[UnitClass])

  var updateCount: Int = 0
  val reserved: Array[Reservation] = Array.fill(With.mapTileWidth * With.mapTileHeight)(Reservation(0, NoPlan(), Seq.empty))

  def update(): Unit = {
    updateCount += 1
    // Clear suggestions
    // Enqueue reservations for deletions
  }
}
