package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.PlacementRequests.PlacementRequest
import Mathematics.Points.Tile
import Planning.Plan
import Planning.Plans.Basic.NoPlan
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case class TileReservation(plan: Plan, target: Tile, update: Int)

class Groundskeeper {
  var updates: Int = 0
  val reserved: Array[TileReservation] = Array.fill(With.mapTileWidth * With.mapTileHeight)(TileReservation(NoPlan(), Tile(0, 0), updates))

  var suggestionsBefore   : mutable.Buffer[PlacementRequest] = ArrayBuffer.empty
  var suggestionsNow      : mutable.Buffer[PlacementRequest] = ArrayBuffer.empty
  var blueprintConsumers  : mutable.Map[Blueprint, FriendlyUnitInfo] = mutable.HashMap.empty

  def update(): Unit = {
    updates += 1
    suggestionsBefore   = suggestionsNow
    suggestionsNow      = ArrayBuffer.empty
    blueprintConsumers.filterNot(_._2.alive).keys.toSeq.foreach(blueprintConsumers.remove)
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

  // I am a building plan. I want to indicate that I have used this blueprint to build something
  def consume(blueprint: Blueprint, unit: FriendlyUnitInfo): Unit = {
    blueprintConsumers(blueprint) = unit
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
    val output = matchSuggestion(plan, unitClass)
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
    output.plan = Some(plan)
    output
  }

  def isReserved(tile: Tile, plan: Option[Plan] = None): Boolean = {
    if ( ! tile.valid) return true
    val reservation = reserved(tile.i)
    if (reservation.update < updates - 1) return false
    if ( ! plan.contains(reservation.plan)) return true
    false
  }

  def reserve(plan: Plan, target: Tile, unitClass: UnitClass): Boolean = {
    reserve(plan, unitClass.tileArea.add(target).tiles)
  }

  def reserve(plan: Plan, tiles: Seq[Tile]): Boolean = {
    val canReserve = tiles.forall(tile => ! isReserved(tile, plan = Some(plan)))
    if (canReserve) {
      tiles.foreach(tile => reserved(tile.i) = TileReservation(plan, tile, updates))
    } else {
      // Where I've seen this, it's been:
      // 1. A plan calls reserve()
      // 2. But it calls it on a tile reserved by a plan with a highier priotiy
      With.logger.warn("Attempting to reserve unreservable tiles " + tiles + " for " + plan)
    }
    canReserve
  }
}
