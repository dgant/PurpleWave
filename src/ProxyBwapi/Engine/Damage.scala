package ProxyBwapi.Engine

import bwapi.{DamageType, UnitSizeType}

object Damage {
  def scaleBySize(damageType: DamageType, sizeType: UnitSizeType): Double =
    damageType match {
      case DamageType.Concussive =>
        sizeType match {
          case UnitSizeType.Large   => 0.25
          case UnitSizeType.Medium  => 0.5
          case _                    => 1.0
        }
      case DamageType.Explosive =>
        sizeType match {
          case UnitSizeType.Small   => 0.5
          case UnitSizeType.Medium  => 0.75
          case _                    => 1.0
        }
      case _ => 1.0
    }
}
