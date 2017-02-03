package Plans

abstract class Plan {
  
  def isComplete():Boolean = { false }
  def children(): Iterable[Plan] = { List.empty }
  def execute() = {}
  def abort() { children().foreach(_.abort()) }
  def describe():Option[String] = { None }
}