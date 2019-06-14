package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.{Blueprint, PlacementRequest}
import Mathematics.Points.Tile
import Planning.Plan
import Planning.Plans.Basic.NoPlan
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable.ArrayBuffer

case class TileReservation(var update: Int, plan: Plan, target: Tile, tiles: Seq[Tile])

class Groundskeeper {
  var updates: Int = 0
  val reserved: Array[TileReservation] = Array.fill(With.mapTileWidth * With.mapTileHeight)(TileReservation(updates, NoPlan(), Tile(0, 0), Seq.empty))

  var suggestionsBefore   : ArrayBuffer[PlacementRequest] = ArrayBuffer.empty
  var suggestionsNow      : ArrayBuffer[PlacementRequest] = ArrayBuffer.empty
  var reservationsBefore  : ArrayBuffer[TileReservation] = ArrayBuffer.empty
  var reservationsNow     : ArrayBuffer[TileReservation] = ArrayBuffer.empty

  def update(): Unit = {
    updates += 1
    suggestionsBefore   = suggestionsNow
    suggestionsNow      = ArrayBuffer.empty
    reservationsBefore  = reservationsNow
    reservationsNow     = ArrayBuffer.empty
  }

  def suggestions: Seq[PlacementRequest] = (suggestionsNow.view ++ suggestionsBefore.view).distinct

  // I am a placer plan. I want to suggest a specific place to put a Gateway.
  def suggest(unitClass: UnitClass, tile: Tile): Unit = {
    val existing = matchSuggestion(unitClass, tile)
    existing.foreach(refresh)
    if (existing.isEmpty) {
      suggestionsNow += new PlacementRequest(new Blueprint(unitClass), tile = Some(tile))
    }
  }

  // I am a placer plan. I want to suggest a blueprint for placing a Gateway.
  def suggest(blueprint: Blueprint): Unit = {
   val existing = matchSuggestion(blueprint)
    existing.foreach(refresh)
    if (existing.isEmpty) {
      suggestionsNow += new PlacementRequest(blueprint)
    }
  }

  // I am a building plan. I want to indicate that I will need placement for a Gateway.
  def suggest(plan: Plan, unitClass: UnitClass) {
    val existing = matchSuggestion(plan, unitClass)
    existing.foreach(refresh)
    if (existing.isEmpty) {
      suggestionsNow += new PlacementRequest(new Blueprint(unitClass), plan = Some(plan))
    }
  }

  private def refresh(request: PlacementRequest): Unit = {
    suggestionsBefore -= request
    if (! suggestionsNow.contains(request)) {
      suggestionsNow += request
    }
  }

  private def matchSuggestion(blueprint: Blueprint): Option[PlacementRequest] = {
    suggestions.find(_.blueprint == blueprint)
  }

  private def matchSuggestion(unitClass: UnitClass, tile: Tile): Option[PlacementRequest] = {
    suggestions.find(s => s.unitClass == unitClass && s.tile.contains(tile))
  }

  private def matchSuggestion(plan: Plan, unitClass: UnitClass): Option[PlacementRequest] = {
    suggestions.find(s => s.unitClass == unitClass && s.plan.forall(_ == plan))
  }

  def getSuggestion(plan: Plan, unitClass: UnitClass): PlacementRequest = {
    matchSuggestion(plan, unitClass)
      .orElse({
        suggest(plan, unitClass)
        matchSuggestion(plan, unitClass)
      })
      .getOrElse({
        With.logger.warn("Failed to find suggestion we JUST issued")
        val output = new PlacementRequest(new Blueprint(unitClass), plan = Some(plan))
        suggestionsNow += output
        output
      })
  }

  def isReserved(tile: Tile, target: Tile, plan: Option[Plan] = None): Boolean = {
    if ( ! tile.valid) return true
    if ( ! target.valid) return true
    val reservation = reserved(tile.i)
    if (reservation.update < updates - 1) return false
    if (reservation.target != target) return true
    if ( ! plan.contains(reservation.plan)) return true
    false
  }

  def reserve(plan: Plan, target: Tile, unitClass: UnitClass): Boolean = {
    reserve(plan, target, unitClass.tileArea.add(target).tiles)
  }

  def reserve(plan: Plan, target: Tile, tiles: Seq[Tile]): Boolean = {
    val destinations = tiles.view :+ target
    val canReserve = destinations.forall(tile => ! isReserved(tile, target, plan = Some(plan)))
    if (canReserve) {
      val reservation = TileReservation(updates, plan, target, tiles)
      reservationsNow += reservation
      destinations.foreach(tile => reserved(tile.i) = reservation)
    } else {
      //With.logger.warn("Attempting to reserve an unreservable tile: " + target + " and " + tiles + " for " + plan)
    }
    canReserve
  }
}
