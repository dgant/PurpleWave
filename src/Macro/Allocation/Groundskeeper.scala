package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.PlacementRequests.PlacementRequest
import Mathematics.Points.Tile
import Performance.Tasks.TimedTask
import Planning.Plans.Basic.NoPlan
import Planning.Prioritized
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Forever

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case class TileReservation(plan: Prioritized, target: Tile, update: Int) {
  def active: Boolean = update >= With.groundskeeper.updates - 1
}

case class RequestReservation(plan: Prioritized, update: Int) {
  def active: Boolean = update >= With.groundskeeper.updates - 1
}

class Groundskeeper extends TimedTask {
  var updates: Int = 0
  val reserved: Array[TileReservation] = Array.fill(With.mapTileWidth * With.mapTileHeight)(TileReservation(NoPlan(), Tile(0, 0), -Forever()))

  var suggestionsBefore   : mutable.Buffer[PlacementRequest] = ArrayBuffer.empty
  var suggestionsNow      : mutable.Buffer[PlacementRequest] = ArrayBuffer.empty
  var blueprintConsumers  : mutable.Map[Blueprint, FriendlyUnitInfo] = mutable.HashMap.empty
  var requestHolders      : mutable.Map[PlacementRequest, RequestReservation] = mutable.HashMap.empty

  override protected def onRun(budgetMs: Long): Unit = {
    updates += 1
    suggestionsBefore   = suggestionsNow
    suggestionsNow      = ArrayBuffer.empty
    blueprintConsumers.view.filterNot(_._2.alive).toSeq.foreach(p => blueprintConsumers.remove(p._1))
    requestHolders.view.filterNot(_._2.active).toSeq.foreach(r => requestHolders.remove(r._1))
  }

  def getRequestHolder(request: PlacementRequest): Option[Prioritized] = {
    requestHolders.get(request).map(_.plan)
  }

  private def setRequestHolder(request: PlacementRequest, holder: Prioritized): Unit = {
    requestHolders(request) = RequestReservation(holder, updates)
  }

  private def suggestionsWithDuplicates: Seq[PlacementRequest] = (suggestionsNow.view ++ suggestionsBefore.view)
    .filterNot(request => blueprintConsumers.contains(request.blueprint))

  def suggestions: Seq[PlacementRequest] = suggestionsWithDuplicates.distinct

  // I am a placer plan. I want to suggest a specific place to put a Gateway.
  def suggest(unitClass: UnitClass, tile: Tile): Unit = {
    val existing = matchSuggestion(unitClass, tile)
    existing.foreach(refresh)
    if (existing.isEmpty) {
      suggestionsNow += new PlacementRequest(new Blueprint(unitClass), tile = Some(tile))
    }
  }

  // I am a placer plan. I want to suggest a specific request for placing an FFE
  def suggest(request: PlacementRequest): Unit = {
    if ( ! suggestionsNow.contains(request)) {
      suggestionsNow += request
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
  def request(requestingPlan: Prioritized, unitClass: UnitClass): PlacementRequest = {
    val matched = matchSuggestion(requestingPlan, unitClass)
    matched.foreach(refresh)
    val output =
      if (matched.isEmpty) {
        val newRequest = new PlacementRequest(new Blueprint(unitClass))
        suggestionsNow += newRequest
        newRequest
      } else {
        matched.get
      }
    setRequestHolder(output, requestingPlan)
    output
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
    suggestionsWithDuplicates.find(_.blueprint == blueprint)
  }

  private def matchSuggestion(unitClass: UnitClass, tile: Tile): Option[PlacementRequest] = {
    // Put the blueprint consumer check at the end because it's the slowest
    suggestionsWithDuplicates.find(s => s.unitClass == unitClass && s.tile.contains(tile))
  }

  private def matchSuggestion(plan: Prioritized, unitClass: UnitClass): Option[PlacementRequest] = {
    // Put the blueprint consumer check at the end because it's the slowest
    suggestionsWithDuplicates.find(s => s.unitClass == unitClass && s.plan.forall(_ == plan))
  }

  def isReserved(tile: Tile, plan: Option[Prioritized] = None): Boolean = {
    if ( ! tile.valid) return true
    val reservation = reserved(tile.i)
    if ( ! reservation.active) return false
    if ( ! plan.contains(reservation.plan)) return true
    false
  }

  def reserve(plan: Prioritized, target: Tile, unitClass: UnitClass): Boolean = {
    reserve(plan, unitClass.tileArea.add(target).tiles)
  }

  def reserve(plan: Prioritized, tiles: Seq[Tile]): Boolean = {
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
