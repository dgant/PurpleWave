package Plans

class Plan {
  
  def isComplete():Boolean = { false }
  def children(): Iterable[Plan] = { List.empty }
  def onFrame() = {}
  def describe():Option[String] = { None }
}