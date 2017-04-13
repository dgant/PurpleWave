package Mathematics.Points

abstract class AbstractPoint(val x:Int, val y:Int) {
  
  def length        : Double = Math.sqrt(lengthSquared)
  def lengthSquared : Double = x*x+y*y
  
  override def toString:String = "[" + x + ", " + y + "]"
}
