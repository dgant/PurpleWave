package Traits

class CascadingProperty[T] {
  private var _parent:Option[CascadingProperty[T]] = None
  private var _value:Option[T] = None
  
  def get:Option[T] = {
    _parent.get.get.getOrElse(_value)
  }
  
  def set(value:T) {
    _value = Some(value)
  }
  def inherit(parent: CascadingProperty[T]) {
    _parent = Some(parent)
  }
  
}
