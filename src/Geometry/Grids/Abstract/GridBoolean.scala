package Geometry.Grids.Abstract

class GridBoolean extends Grid[Boolean] {
  
  override val _positions:Array[Boolean] = Array.fill(_width * _height)(_defaultValue)
  override def _defaultValue:Boolean = false
  override def repr(value: Boolean) = value.toString
}
