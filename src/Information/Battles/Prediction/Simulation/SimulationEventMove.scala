package Information.Battles.Prediction.Simulation

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Points.Pixel

case class SimulationEventMove(
  frame     : Int,
  sim       : Simulacrum,
  pixelFrom : Pixel,
  pixelTo   : Pixel,
  frames    : Int)
    extends SimulationEvent {

  override def toString: String = Vector(
    frame + ":",
    describe(sim),
    "moves from",
    pixelFrom,
    "to",
    pixelTo,
    "in",
    frames,
    "frames"
    ).map(_.toString).mkString(" ")

  override def draw() {
    DrawMap.arrow(pixelFrom, pixelTo, sim.realUnit.player.colorMedium)
  }

  override def from: Pixel = pixelFrom
  override def to: Pixel = pixelFrom
}
