package Information.Grids.Abstract

class GridBoolean extends GridArray[Boolean] {
  
  override val _positions:Array[Boolean] = Array.fill(_width * _height)(defaultValue)
  override def defaultValue:Boolean = false
  override def repr(value: Boolean) = value.toString
}
