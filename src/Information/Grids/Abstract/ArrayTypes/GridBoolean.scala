package Information.Grids.Abstract.ArrayTypes

class GridBoolean extends GridArray[Boolean] {
  
  override var values:Array[Boolean] = Array.fill(width * height)(defaultValue)
  override def defaultValue:Boolean = false
  override def repr(value: Boolean) = value.toString
}
