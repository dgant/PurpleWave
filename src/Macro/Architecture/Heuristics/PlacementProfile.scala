package Macro.Architecture.Heuristics


class PlacementProfile(
  val name            : String,
  var preferZone      : Double = 0.0,
  var preferExit      : Double = 0.0,
  var preferGas       : Double = 0.0,
  var preferSpace     : Double = 0.0,
  var preferPowering  : Double = 0.0,
  var avoidExit       : Double = 0.0,
  var avoidDistance   : Double = 0.0,
  var avoidEnemy      : Double = 0.0) {
  
  def weightedHeuristics: Iterable[PlacementHeuristicWeight] = {
    Vector(
      new PlacementHeuristicWeight(PlacementHeuristicZone,          preferZone),
      new PlacementHeuristicWeight(PlacementHeuristicExit,          preferExit),
      new PlacementHeuristicWeight(PlacementHeuristicGas,           preferGas),
      new PlacementHeuristicWeight(PlacementHeuristicSpace,         preferSpace),
      new PlacementHeuristicWeight(PlacementHeuristicPowering,      preferPowering),
      new PlacementHeuristicWeight(PlacementHeuristicExit,         -avoidExit),
      new PlacementHeuristicWeight(PlacementHeuristicDistance,     -avoidDistance),
      new PlacementHeuristicWeight(PlacementHeuristicEnemy,        -avoidEnemy)
    )
  }
  
  override def toString: String = name
}
