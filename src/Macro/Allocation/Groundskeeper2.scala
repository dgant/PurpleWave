package Macro.Allocation

import Lifecycle.With
import Mathematics.Points.Tile
import Planning.Plan
import Planning.Plans.Basic.NoPlan
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable.ArrayBuffer

class Groundskeeper2 {

  case class Suggestion(tile: Tile, unitClasses: UnitClass*)
  case class Reservation(var update: Int, plan: Plan, target: Tile, tiles: Seq[Tile], unitClasses: UnitClass*)

  var updates: Int = 0
  val reserved: Array[Reservation] = Array.fill(With.mapTileWidth * With.mapTileHeight)(Reservation(updates, NoPlan(), Tile(0, 0), Seq.empty))

  var suggestionsBefore   : ArrayBuffer[Suggestion] = ArrayBuffer.empty
  var suggestionsNow      : ArrayBuffer[Suggestion] = ArrayBuffer.empty
  var reservationsBefore  : ArrayBuffer[Reservation] = ArrayBuffer.empty
  var reservationsNow     : ArrayBuffer[Reservation] = ArrayBuffer.empty

  def update(): Unit = {
    updates += 1
    suggestionsBefore = suggestionsNow
    suggestionsNow = ArrayBuffer.empty
    reservationsBefore = reservationsNow
    reservationsNow = ArrayBuffer.empty
  }

  def suggest(suggestion: Suggestion): Unit = {
    suggestionsNow.append(suggestion)
  }

  def getSuggestion(unitClass: UnitClass): Option[Tile] = {
    suggestionsNow.find(_.unitClasses.contains(unitClass)).orElse(
      suggestionsBefore.find(_.unitClasses.contains(unitClass)))
      .map(_.tile)
  }

  def isFree(tile: Tile, target: Tile, unitClass: UnitClass = null, plan: Plan = null): Boolean = {
    if (!tile.valid) return false
    if (!target.valid) return false
    val reservation = reserved(tile.i)
    if (reservation.update < updates - 1) return true
    if (reservation.target != target) return false
    if (plan != null && plan != reservation.plan) return false
    if (unitClass != null && ! reservation.unitClasses.contains(unitClass)) return false
    true
  }

  def reserve(reservation: Reservation): Boolean = {
    reservation.update = updates
    val destinations = reservation.tiles.view :+ reservation.target
    val canReserve = destinations.forall(tile => isFree(tile, reservation.target, plan = reservation.plan))
    if (canReserve) {
      reservationsNow += reservation
      destinations.foreach(tile => reserved(tile.i) = reservation)
    }
    canReserve
  }
}
