package Micro.Heuristics.MovementHeuristics
import Mathematics.Heuristics.HeuristicMath
import Micro.Intent.Intention
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import bwapi.TilePosition

object MovementHeuristicAttackSpeed extends MovementHeuristic {
  
  // According to https://docs.google.com/spreadsheets/d/1bsvPvFil-kpvEUfSG74U3E5PLSTC02JxSkiR8QdLMuw/edit#gid=0
  // some units fire faster when they don't move in between attacks.
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
    
    if (intent.unit.cooldownLeft == 0) return HeuristicMath.default
    
    val relevantUnits = List(Terran.Marine, Terran.Ghost, Protoss.Zealot, Protoss.Dragoon, Zerg.Hydralisk)
    if ( ! relevantUnits.contains(intent.unit.unitClass)) return HeuristicMath.default
    
    HeuristicMath.fromBoolean(intent.unit.tileTopLeft == candidate)
  }
}
