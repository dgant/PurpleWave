package Macro.SimCity

import Lifecycle.With
import Mathematics.Points.Tile

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Groundskeeper {
  
  // Any Plan can request a building placement. This means "It'd be nice if a building were placed here"
  // Any Plan can reserve a building placement. This means "I intend to place a building here."
  //
  // SUGGEST:
  // Jot down the request.
  //
  // RESERVE:
  //
  // 1. If there's an existing matching request that isn't reserved, we use that request.
  // Otherwise, we issue a new building placement request.
  //
  // 2. If the request has been fulfilled, we return the chosen Tile.
  // Otherwise, we return None.
  //
  // RUN:
  //
  // 3. Try to fulfill requests in priority order.
  // If a request was previously fulfilled, we make sure it's still valid. If it's not, we try again.
  // For each fulfilled request, we create an exclusion preventing lower-priority requests from using the same area.
  //
  // 4. TODO: What do we do when we run out of Pylon power?
  // I think we'd like to indicate, somehow, that we are in Pylon distress, and throw up some Pylons accordingly.
  
  private val suggestions   = new ArrayBuffer[BuildingDescriptor]
  private val reservations  = new ArrayBuffer[BuildingDescriptor]
  private val placements    = new mutable.HashMap[BuildingDescriptor, Tile]
  
  def run() {
    
    // Do placements sequentially.
    //
    // TODO: If we run out of time, do we:
    //
    // 1. [x] Start placement from the beginning? (potentially delaying low-priority placements)
    // 2. [ ] Pause and pick up placement next time? (potentially leading to stale placements)
    // 3. [ ] Overengineer an urgency-based queue for placements? (no)
    
    val architect = new Architect
    suggestions.foreach(suggestion => {
      if (With.performance.continueRunning) {
        val tile = architect.fulfill(suggestion, placements.get(suggestion))
        if (tile.isDefined) placements.put(suggestion, tile.get) else placements.remove(suggestion)
      }
    })
  }
  
  def suggest(descriptor: BuildingDescriptor) {
    
    //TODO: How do we replace the last descriptor this plan requested?
    //TODO: How do we identify abandoned descriptors?
    
    suggestions.append(descriptor)
  }
  
  def reserve(descriptor: BuildingDescriptor): Option[Tile] = {
    val suggestion = findAvailableSuggestion(descriptor)
    if (suggestion.isDefined) {
      getPlacement(suggestion.get)
    } else {
      suggest(descriptor)
      None
    }
  }
  
  private def findAvailableSuggestion(descriptor: BuildingDescriptor): Option[BuildingDescriptor] = {
    
    // TODO: Actually prioritize
    val suggestionsInPriorityOrder = suggestions
    
    suggestionsInPriorityOrder.find(
      suggestion =>
        ! isReserved(descriptor) &&
        descriptor.fulfilledBy(suggestion))
  }
  
  private def getPlacement(descriptor: BuildingDescriptor): Option[Tile] = {
    placements.get(descriptor)
  }
  
  private def isReserved(descriptor: BuildingDescriptor): Boolean = {
    
    // TODO: Make O(1)
    reservations.contains(descriptor)
  }
}
