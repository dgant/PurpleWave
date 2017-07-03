package Strategery.Strategies

abstract class Strategy() {
  
  override def toString: String = getClass.getSimpleName.replace("$", "")
}
