package Traits

trait TraitSettableSatisfaction {
  var _isSatisfied:Boolean = false
  def getSatisfaction:Boolean = { _isSatisfied }
  def setSatisfaction(value:Boolean) { _isSatisfied = value }
}
