package ProxyBwapi

import ProxyBwapi.Size.{Large, Medium}

object Damage {
  
  trait Type {
    val damageAgainstLarge : Double = this match {
      case Concussive => 0.25
      case _          => 1.0
    }
    val damageAgainstMedium: Double = this match {
      case Concussive => 0.5
      case Explosive  => 0.75
      case _          => 1.0
    }
    val damageAgainstSmall: Double  = this match {
      case Concussive => 1.0
      case Explosive  => 0.25
      case _          => 0.5
    }

    @inline final def apply(size: Size.Type): Double =
      if      (size == Large)   damageAgainstLarge
      else if (size == Medium)  damageAgainstMedium
      else                      damageAgainstSmall
  }
  
  object Normal     extends Type
  object Concussive extends Type
  object Explosive  extends Type
}
