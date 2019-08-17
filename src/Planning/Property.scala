package Planning

class Property[T](default: T) {
  
  private var _parent: Option[Property[T]] = None
  private var _value: T = default
  private var _set: Boolean = false

  def isSet: Boolean = _set
  
  def get: T = {
    if (_parent.contains(this)) {
      throw new Exception("Cyclical inheritance for " + this.getClass.toString)
    }
    _parent.map(_.get).getOrElse(_value)
  }
  
  def inherit(parent: Property[T]) {
    if (parent == this) {
      throw new Exception("Tried to assign cyclical inheritance for " + this.getClass.toString)
    }
    
    _parent = Some(parent)
  }
  
  def apply(): T = get
  
  def reset() {
    set(default)
    _set = false
  }
  
  def set(value: T) {
    _value = value
    _set = true
  }
  
  override def toString: String = "Property: " + get.toString
}
