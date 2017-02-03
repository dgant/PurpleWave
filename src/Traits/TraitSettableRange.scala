package Traits

trait TraitSettableRange {
  var _range:Integer = 32
  def getRange:Integer = { _range }
  def setRange(value:Integer) = { _range = value }
}
