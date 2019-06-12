package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile
import Planning.Plan
import Planning.Plans.Basic.NoPlan
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable.ArrayBuffer

class Groundskeeper {

  case class TileSuggestion(tile: Tile, unitClasses: Seq[UnitClass])
  case class TileReservation(var update: Int, plan: Plan, target: Tile, tiles: Seq[Tile], unitClasses: Seq[UnitClass])

  var updates: Int = 0
  val reserved: Array[TileReservation] = Array.fill(With.mapTileWidth * With.mapTileHeight)(TileReservation(updates, NoPlan(), Tile(0, 0), Seq.empty, Seq.empty))

  var proposalsBefore     : ArrayBuffer[Blueprint] = ArrayBuffer.empty
  var proposalsNow        : ArrayBuffer[Blueprint] = ArrayBuffer.empty
  var suggestionsBefore   : ArrayBuffer[TileSuggestion] = ArrayBuffer.empty
  var suggestionsNow      : ArrayBuffer[TileSuggestion] = ArrayBuffer.empty
  var reservationsBefore  : ArrayBuffer[TileReservation] = ArrayBuffer.empty
  var reservationsNow     : ArrayBuffer[TileReservation] = ArrayBuffer.empty

  def update(): Unit = {
    updates += 1
    proposalsBefore     = proposalsNow
    proposalsNow        = ArrayBuffer.empty
    suggestionsBefore   = suggestionsNow
    suggestionsNow      = ArrayBuffer.empty
    reservationsBefore  = reservationsNow
    reservationsNow     = ArrayBuffer.empty
  }

  def propose(blueprint: Blueprint): Unit = {
    Placer.placeBlueprints(blueprint)
      .foreach(placement =>
        placement.tile.foreach(tile =>
          suggest(tile, placement.blueprint.building.toSeq: _*)))
  }

  def suggest(tile: Tile, unitClasses: UnitClass*): Unit = {
    suggestionsNow.append(TileSuggestion(tile, unitClasses))
  }

  def getSuggestion(unitClass: UnitClass): Option[Tile] = {
    suggestionsNow.find(_.unitClasses.contains(unitClass)).orElse(
      suggestionsBefore.find(_.unitClasses.contains(unitClass)))
      .map(_.tile)
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
