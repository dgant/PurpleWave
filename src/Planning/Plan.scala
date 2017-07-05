package Planning

import Lifecycle.With
import Planning.Composition.Property

class Plan {
  
  var parent: Option[Plan] = None
  
  
  def isComplete: Boolean = false
  def getChildren: Iterable[Plan] = Vector.empty
  def visualize(): Unit = {}
  
  protected def onUpdate() {}
  final def update() {
    With.prioritizer.prioritize(this)
    onUpdate()
  }
  
  final def delegate(child: Plan) {
    child.parent = Some(this)
    child.update()
  }
  
  lazy val realName: String = {
    val name = getClass.getSimpleName
    if (name.contains("$anon$")) "" else name
  }
  
  val description = new Property[String](realName)
  
  override def toString: String =
    if (realName == "")
      description.get
    else
      if (description.get == "") realName else description.get
  
  def isPrioritized: Boolean = With.prioritizer.isPrioritized(this)
  def priority: Int = With.prioritizer.getPriority(this)
}