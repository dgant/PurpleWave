package Types.Plans

abstract class Plan {
  var _children:Iterable[Plan] = List.empty
  
  def isComplete():Boolean = { false }
  def children(): Iterable[Plan] = { _children }
  def execute()
  def abort() { children().foreach(_.abort()) }
}