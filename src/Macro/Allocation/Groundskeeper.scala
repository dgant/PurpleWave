package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Mathematics.Points.Tile
import Planning.Plan

import scala.collection.mutable

class Groundskeeper {

  private val updated     = new mutable.HashSet[BuildingDescriptor]
  private val available   = new mutable.HashSet[BuildingDescriptor]
  private val reserved    = new mutable.HashMap[BuildingDescriptor, Plan]
  private val placements  = new mutable.HashMap[BuildingDescriptor, Tile]
  
  def update() {
    available.diff(updated).foreach(removeDescriptor)
    reserved.keySet.diff(updated).foreach(removeDescriptor)
    updated.clear()
  }
  
  def placeBuildings() {
    With.architect.reboot()
    Vector(reserved.keys, available.diff(reserved.keySet))
      .foreach(descriptors => sortByPriority(descriptors)
        .foreach(suggestion =>
          if (With.performance.continueRunning) {
            val tile = With.architect.fulfill(suggestion, placements.get(suggestion))
            if (tile.isDefined) placements.put(suggestion, tile.get) else placements.remove(suggestion)
          }))
  }
  
  def suggest(descriptor: BuildingDescriptor) {
    available += descriptor
    updated   += descriptor
  }
  
  def reserve(descriptor: BuildingDescriptor): Option[Tile] = {
    val matchingSuggestion = sortByPriority(available).find(descriptor.fulfilledBy)
    if (matchingSuggestion.isDefined) {
      available.remove(matchingSuggestion.get)
      reserved.put(matchingSuggestion.get, descriptor.suggestor)
      placements.get(matchingSuggestion.get)
    } else {
      suggest(descriptor)
      None
    }
  }
  
  private def sortByPriority(descriptors: Iterable[BuildingDescriptor] ): Iterable[BuildingDescriptor] = {
    descriptors.toVector.sortBy(suggestion => With.prioritizer.getPriority(suggestion.suggestor))
  }
  
  private def removeDescriptor(descriptor: BuildingDescriptor) {
    available   -=  descriptor
    reserved    -=  descriptor
    placements  -=  descriptor
  }
}
