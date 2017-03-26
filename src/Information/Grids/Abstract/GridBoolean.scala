package Information.Grids.Abstract

class GridBoolean extends GridArray[Boolean] {
  
  override val values:Array[Boolean] = Array.fill(width * height)(defaultValue)
  override def defaultValue:Boolean = false
  override def repr(value: Boolean) = value.toString
}
