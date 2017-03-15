package Planning

import Planning.Composition.Property

class Plan {
  val description = new Property[String]("")
  
  def isComplete:Boolean = false
  def getChildren:Iterable[Plan] = List.empty
  def onFrame() = {}
  def drawOverlay() = {}
  
  override def toString: String = getRealName + (if (description.get == "") "" else ": " + description.get)
  
  private def getRealName:String = {
    val name = getClass.getSimpleName
    if (name.contains("$anon$")) "" else name
  }
}