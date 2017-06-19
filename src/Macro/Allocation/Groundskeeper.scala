package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.{BuildingDescriptor, Placement}
import Mathematics.Points.Tile

import scala.collection.mutable

class Groundskeeper {

  val updated   : mutable.Set[BuildingDescriptor]             = new mutable.HashSet[BuildingDescriptor]
  val unplaced  : mutable.Set[BuildingDescriptor]             = new mutable.HashSet[BuildingDescriptor]
  val placed    : mutable.Map[BuildingDescriptor, Placement]  = new mutable.HashMap[BuildingDescriptor, Placement]
  
  def update() {
    if (updated.isEmpty) {
      return
    }
    unplaced.diff(updated).foreach(removeDescriptor)
    placed.keySet.diff(updated).foreach(removeDescriptor)
    updated.clear()
  }
  
  def placeBuildings() {
    With.architect.reboot()
    Vector(placed.keys, unplaced)
      .foreach(descriptors => sortByPriority(descriptors)
        .foreach(descriptor =>
          if (With.performance.continueRunning) {
            val tile = With.architect.fulfill(descriptor, placed.get(descriptor))
            placed.remove(descriptor)
            tile
              .map(
                Placement(
                  descriptor,
                  _,
                  With.frame))
              .foreach(placed.put(descriptor, _))
          }))
  }
  
  def suggest(descriptor: BuildingDescriptor) {
    updated += descriptor
    if ( ! placed.contains(descriptor)) {
      unplaced += descriptor
    }
  }
  
  def reserve(descriptor: BuildingDescriptor): Option[Tile] = {
    
    updated += descriptor
    
    // Do we have a placement for this descriptor already? Use it.
    placed
      .get(descriptor)
      .foreach(placement => {
        unplaced -= descriptor
        return Some(placement.tile)
      })
    
    /*
    // Do we have a placement for a matching descriptor? Use that placement.
    sortByPriority(placed.keys)
      .find(descriptor.fulfilledBy)
      .foreach(matchingPlacement =>
        return placed.put(descriptor, placed(matchingPlacement)))
    */
    
    // This request is a new suggestion. Add it.
    suggest(descriptor)
    None
  }
  
  def sortByPriority(descriptors: Iterable[BuildingDescriptor] ): Iterable[BuildingDescriptor] = {
    descriptors.toVector.sortBy(suggestion => With.prioritizer.getPriority(suggestion.suggestor))
  }
  
  private def removeDescriptor(descriptor: BuildingDescriptor) {
    unplaced  -=  descriptor
    placed    -=  descriptor
  }
}
