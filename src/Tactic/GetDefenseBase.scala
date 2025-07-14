package Tactic

import Information.Geography.Types.Base

object GetDefenseBase {
  def apply(base: Base): Base = {
    base.natural.filter(b => b.isOurs || b.plannedExpoRecently).getOrElse(base)
  }
}
