package Traits

class Property[T](default:T) {
  
  private var _parent:Option[Property[T]] = None
  private var _value:T = default
  
  def get:T = {
    _parent.map(_.get).getOrElse(_value)
  }
  
  def set(value:T) {
    _value = value
  }
  def inherit(parent: Property[T]) {
    _parent = Some(parent)
  }
  
}
