package Micro.Heuristics

object HeuristicMath {
  

  
  def unboolify(value:Boolean):Double = if (value) 2.0 else 1.0
  
  val heuristicMaximum = 1000000.0
  val heuristicMinimum = 1/heuristicMaximum
  def normalize(value:Double) = Math.min(Math.max(heuristicMinimum, value), heuristicMaximum)
}
