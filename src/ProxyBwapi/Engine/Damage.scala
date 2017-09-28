package ProxyBwapi.Engine

object Damage {
  
  trait Type {
    def ratioAgainst(sizeType: Size.Type): Double = Damage.scaleBySize(this, sizeType)
  }
  
  object Normal     extends Type
  object Concussive extends Type
  object Explosive  extends Type
  
  def scaleBySize(damageType: Type, sizeType: Size.Type): Double =
    damageType match {
      case Concussive =>
        sizeType match {
          case Size.Large   => 0.25
          case Size.Medium  => 0.5
          case _            => 1.0
        }
      case Explosive =>
        sizeType match {
          case Size.Small   => 0.5
          case Size.Medium  => 0.75
          case _            => 1.0
        }
      case _ => 1.0
    }
}
