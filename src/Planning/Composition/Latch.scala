package Planning.Composition

import Planning.Plan

class Latch (predicate: Plan) extends Plan{
  
  var completedOnce: Boolean= false
  
  override def isComplete: Boolean = completedOnce|| predicate.isComplete
  override def onUpdate(): Unit = if ( ! isComplete) predicate.update()
  override def getChildren: Iterable[Plan] = Iterable(predicate)
  
}
