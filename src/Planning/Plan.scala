package Planning

import Lifecycle.With

class Plan extends Prioritized {
  
  val description = new Property[String](realName)
  val frameCreated = With.frame
  
  def isComplete: Boolean = false
  def getChildren: Iterable[Plan] = Vector.empty
  
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

  def isEmpty: Boolean = false

  val id: Long = PlanId()
  object PlanId {
    var id: Long = 0
    def apply(): Long = { id += 1;  id - 1 }
  }
}