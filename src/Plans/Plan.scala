package Plans

import Types.Property

class Plan {
  val description = new Property[Option[String]](None)
  
  def isComplete:Boolean = { false }
  def getChildren:Iterable[Plan] = { List.empty }
  def onFrame() = {}
  def drawOverlay() = { }
  
  override def toString: String = { _getRealName ++ description.get.getOrElse("") }
  def _getRealName:String = {
    val name = this.getClass.getSimpleName
    if (name.contains("$anon$")) "-> " else (name + ": ")
  }
}