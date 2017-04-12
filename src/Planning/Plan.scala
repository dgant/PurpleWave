package Planning

import Planning.Composition.Property

class Plan {
  val description = new Property[String]("")
  
  def isComplete:Boolean = false
  def getChildren:Iterable[Plan] = Vector.empty
  def update() = {}
  def drawOverlay() = {}
  
  override def toString: String =
    if (realName == "")
      description.get
    else
      if (description.get == "") realName else description.get
  
  lazy val realName:String = {
    val name = getClass.getSimpleName
    if (name.contains("$anon$")) "" else name
  }
}