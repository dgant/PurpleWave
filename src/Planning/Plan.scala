package Planning

import Lifecycle.With

class Plan {
  
  val description = new Property[String](realName)
  
  def isComplete: Boolean = false
  def getChildren: Iterable[Plan] = Vector.empty
  def visualize(): Unit = {}
  
  protected def onUpdate() {}
  
  final var parent: Option[Plan] = None
  
  final def update() {
    With.prioritizer.prioritize(this)
    onUpdate()
  }
  
  final def delegate(child: Plan) {
    child.parent = Some(this)
    child.update()
  }
  
  final lazy val realName: String = {
    val name = getClass.getSimpleName
    if (name.contains("$anon$")) "" else name
  }
  
  final override def toString: String =
    if (realName == "")
      description.get
    else
      if (description.get == "") realName else description.get
  
  final def isPrioritized: Boolean = With.prioritizer.isPrioritized(this)
  final def priority: Int = With.prioritizer.getPriority(this)

  def isEmpty: Boolean = false
}