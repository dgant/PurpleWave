package Information.Battles.Prediction.Simulation
import java.text.DecimalFormat

import Mathematics.Points.Pixel

case class SimulationEventSwitchTarget(unit: Simulacrum, targetBefore: Option[Simulacrum], targetAfter: Option[Simulacrum]) extends SimulationEvent(unit) {
  override def to: Pixel = from

  private val formatter = new DecimalFormat("#.##")
  private val pBefore = targetBefore.map(_.pixel)
  private val pAfter = targetAfter.map(_.pixel)
  private val dBefore = targetBefore.map(unit.pixelDistanceEdge(_) / 32.0).getOrElse(0.0)
  private val dAfter = targetAfter.map(unit.pixelDistanceEdge(_) / 32.0).getOrElse(0.0)
  private def describeOpt(unit: Simulacrum, target: Option[Simulacrum], at: Option[Pixel], distance: Double): String =
    target
      .map(t => f"${describe(t)} ${describePixel(at)} @ ${formatter.format(distance)} tiles")
      .getOrElse("(Nobody)")

  override def toString: String = f"$frame: ${describe(unit)} changes target from ${describeOpt(unit, targetBefore, pBefore, dBefore)} to ${describeOpt(unit, targetAfter, pAfter, dAfter)}"
}
