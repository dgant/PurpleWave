package Plans

import Traits.Property

class Plan {
  val description = new Property[Option[String]](None)
  
  def isComplete:Boolean = { false }
  def getChildren:Iterable[Plan] = { List.empty }
  def onFrame() = {}
  def drawOverlay() = { }
}