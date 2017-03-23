package Micro.Heuristics.TileHeuristics
import Micro.Intentions.Intention
import bwapi.TilePosition

object TileHeuristicDestinationNearby extends TileHeuristic {
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    if (intent.destination.isEmpty) return 1.0
  
    //Incentivize longer-ranged units to get closer
    //TODO: We should use an effective range for spellcasters
    //TODO: This is overloading the concept of "go to place" and "be in reasonable order for combat"
    val range = intent.unit.unitClass.maxAirGroundRange
    
    val before = intent.unit.travelPixels(intent.unit.tileCenter,  intent.destination.get) - range
    val after  = intent.unit.travelPixels(candidate,               intent.destination.get) - range
  
    
    val threshold = 32.0 * 4.0
    if (before < threshold || after < threshold) return 1.0
  
    return before/after
    
  }
}
