package Plans.Generic.Compound

import Plans.Plan

abstract class PlanWithSettableListOfChildren extends Plan {
  
  var kids:List[Plan] = List.empty
  
  override def children():Iterable[Plan] = { kids }
}
