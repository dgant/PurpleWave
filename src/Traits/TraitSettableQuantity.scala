package Traits

trait TraitSettableQuantity {
  
  var _quantity:Integer = 1
  
  def getQuantity:Integer = {
    _quantity
  }
  
  def setQuantity(quantity:Integer) = {
    _quantity = quantity
  }
}
