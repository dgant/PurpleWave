package Traits

trait TraitSettableDescription {
  var _description:Option[String] = None
  
  def getDescription:Option[String] = {
    _description
  }
  
  def setDescription(value:String) {
    _description = Some(value)
  }
}
