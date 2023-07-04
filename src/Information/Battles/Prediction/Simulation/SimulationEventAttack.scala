package Information.Battles.Prediction.Simulation

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Points.Pixel

final case class SimulationEventAttack(
  shooter : Simulacrum,
  victim  : Simulacrum,
  damage  : Int,
  fatal   : Boolean)
    extends SimulationEvent(shooter) {
  
  private val victimPixel = victim.pixel
  private val victimShields = victim.shieldPoints
  private val victimHp = victim.hitPoints

  override val to: Pixel = from
  
  override def toString: String =
    f"$frame: ${shooter.describe} ${shooter.pixel} strikes ${victim.describe} ${victim.pixel} for $damage damage ${
      if (fatal) "(Fatal)"
      else f"leaving ${if (victim.shieldPointsInitial > 0) f"$victimShields shields + " else ""}$victimHp hp"}"
  
  override def draw() {
    val size = 5
    val color = victim.realUnit.player.colorBright
    DrawMap.line(victimPixel.add(-5, -5), victimPixel.add(5, 5),  color)
    DrawMap.line(victimPixel.add(5, -5),  victimPixel.add(-5, 5), color)
  }
}
