package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.{Blueprint, PlacementSuggestion}
import Mathematics.Points.Tile
import Planning.Plan
import Planning.Plans.Basic.NoPlan
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable.ArrayBuffer

class Groundskeeper {
  case class TileReservation(var update: Int, plan: Plan, target: Tile, tiles: Seq[Tile], unitClasses: Seq[UnitClass])

  var updates: Int = 0
  val reserved: Array[TileReservation] = Array.fill(With.mapTileWidth * With.mapTileHeight)(TileReservation(updates, NoPlan(), Tile(0, 0), Seq.empty, Seq.empty))

  var suggestionsBefore   : ArrayBuffer[PlacementSuggestion] = ArrayBuffer.empty
  var suggestionsNow      : ArrayBuffer[PlacementSuggestion] = ArrayBuffer.empty
  var reservationsBefore  : ArrayBuffer[TileReservation] = ArrayBuffer.empty
  var reservationsNow     : ArrayBuffer[TileReservation] = ArrayBuffer.empty

  def update(): Unit = {
    updates += 1
    suggestionsBefore   = suggestionsNow
    suggestionsNow      = ArrayBuffer.empty
    reservationsBefore  = reservationsNow
    reservationsNow     = ArrayBuffer.empty
  }

  def suggestions: Seq[PlacementSuggestion] = suggestionsNow.view ++ suggestionsBefore.view

  def suggest(blueprint: Blueprint): Unit = {
    suggestionsNow.append(new PlacementSuggestion(blueprint.building.get, blueprint))
  }

  def suggest(tile: Tile, unitClass: UnitClass): Unit = {
    suggestionsNow.append(new PlacementSuggestion(unitClass, tile))
  }

  def getSuggestion(unitClass: UnitClass): PlacementSuggestion = {
    suggestions.find(_.building == unitClass).getOrElse(new PlacementSuggestion(unitClass, new Blueprint(Some(unitClass))))
  }

  def isReserved(tile: Tile, target: Tile, unitClass: UnitClass = null, plan: Plan = null): Boolean = {
    if (!tile.valid) return true
    if (!target.valid) return true
    val reservation = reserved(tile.i)
    if (reservation.update < updates - 1) return false
    if (reservation.target != target) return true
    if (plan != null && plan != reservation.plan) return true
    if (unitClass != null && ! reservation.unitClasses.contains(unitClass)) return true
    false
  }

  def reserve(plan: Plan, target: Tile, unitClass: UnitClass): Boolean = {
    reserve(plan, target, unitClass.tileArea.add(target).tiles, unitClass)
  }

  def reserve(plan: Plan, target: Tile, tiles: Seq[Tile], unitClasses: UnitClass*): Boolean = {
    val reservation = TileReservation(updates, plan, target, tiles, unitClasses)
    val destinations = reservation.tiles.view :+ reservation.target
    val canReserve = destinations.forall(tile => isReserved(tile, reservation.target, plan = reservation.plan))
    if (canReserve) {
      reservationsNow += reservation
      destinations.foreach(tile => reserved(tile.i) = reservation)
    }
    canReserve
  }
}
